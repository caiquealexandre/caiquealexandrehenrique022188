package br.gov.mt.seletivo_seplag.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.gov.mt.seletivo_seplag.domain.service.RegionaisSyncService.SyncResult;
import br.gov.mt.seletivo_seplag.infra.persistence.entity.RegionalEntity;
import br.gov.mt.seletivo_seplag.infra.persistence.repository.RegionalRepository;
import br.gov.mt.seletivo_seplag.infra.regionais.RegionaisClientFeign;
import br.gov.mt.seletivo_seplag.infra.regionais.RegionalExternalDto;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegionaisSyncServiceTest {

  @Mock
  private RegionaisClientFeign client;

  @Mock
  private RegionalRepository repo;

  @InjectMocks
  private RegionaisSyncService service;

  @Nested
  @DisplayName("sync()")
  class SyncTests {

    @Test
    @DisplayName("deve inserir novas regionais")
    void shouldInsertNewRegionais() {
      RegionalExternalDto ext1 = new RegionalExternalDto(1, "Regional Norte");
      RegionalExternalDto ext2 = new RegionalExternalDto(2, "Regional Sul");

      when(client.fetchAll()).thenReturn(List.of(ext1, ext2));
      when(repo.findByAtivoTrue()).thenReturn(List.of());

      SyncResult result = service.sync();

      assertThat(result.inserido()).isEqualTo(2);
      assertThat(result.inativado()).isEqualTo(0);
      assertThat(result.versionado()).isEqualTo(0);
      assertThat(result.contagemExterna()).isEqualTo(2);

      ArgumentCaptor<List<RegionalEntity>> captor = ArgumentCaptor.forClass(List.class);
      verify(repo).saveAll(captor.capture());

      List<RegionalEntity> saved = captor.getValue();
      assertThat(saved).hasSize(2);
      assertThat(saved.get(0).getExternalId()).isEqualTo(1);
      assertThat(saved.get(0).getNome()).isEqualTo("Regional Norte");
      assertThat(saved.get(0).getAtivo()).isTrue();
    }

    @Test
    @DisplayName("deve inativar regionais ausentes no externo")
    void shouldInactivateAbsentRegionais() {
      RegionalEntity existing = new RegionalEntity();
      existing.setId(1L);
      existing.setExternalId(1);
      existing.setNome("Regional Norte");
      existing.setAtivo(true);

      when(client.fetchAll()).thenReturn(List.of());
      when(repo.findByAtivoTrue()).thenReturn(List.of(existing));

      SyncResult result = service.sync();

      assertThat(result.inserido()).isEqualTo(0);
      assertThat(result.inativado()).isEqualTo(1);
      assertThat(result.versionado()).isEqualTo(0);

      ArgumentCaptor<List<RegionalEntity>> captor = ArgumentCaptor.forClass(List.class);
      verify(repo).saveAll(captor.capture());

      List<RegionalEntity> saved = captor.getValue();
      assertThat(saved).hasSize(1);
      assertThat(saved.get(0).getAtivo()).isFalse();
    }

    @Test
    @DisplayName("deve versionar quando nome muda")
    void shouldVersionWhenNameChanges() {
      RegionalEntity existing = new RegionalEntity();
      existing.setId(1L);
      existing.setExternalId(1);
      existing.setNome("Nome Antigo");
      existing.setAtivo(true);

      RegionalExternalDto ext = new RegionalExternalDto(1, "Nome Novo");

      when(client.fetchAll()).thenReturn(List.of(ext));
      when(repo.findByAtivoTrue()).thenReturn(List.of(existing));

      SyncResult result = service.sync();

      assertThat(result.inserido()).isEqualTo(0);
      assertThat(result.inativado()).isEqualTo(1);
      assertThat(result.versionado()).isEqualTo(1);

      ArgumentCaptor<List<RegionalEntity>> captor = ArgumentCaptor.forClass(List.class);
      verify(repo).saveAll(captor.capture());

      List<RegionalEntity> saved = captor.getValue();
      assertThat(saved).hasSize(2);

      RegionalEntity inactivated = saved.stream()
          .filter(r -> r.getId() != null && r.getId().equals(1L))
          .findFirst()
          .orElseThrow();
      assertThat(inactivated.getAtivo()).isFalse();

      RegionalEntity newVersion = saved.stream()
          .filter(r -> r.getId() == null)
          .findFirst()
          .orElseThrow();
      assertThat(newVersion.getNome()).isEqualTo("Nome Novo");
      assertThat(newVersion.getAtivo()).isTrue();
    }

    @Test
    @DisplayName("nao deve alterar quando nome eh igual")
    void shouldNotChangeWhenNameIsEqual() {
      RegionalEntity existing = new RegionalEntity();
      existing.setId(1L);
      existing.setExternalId(1);
      existing.setNome("Regional Norte");
      existing.setAtivo(true);

      RegionalExternalDto ext = new RegionalExternalDto(1, "Regional Norte");

      when(client.fetchAll()).thenReturn(List.of(ext));
      when(repo.findByAtivoTrue()).thenReturn(List.of(existing));

      SyncResult result = service.sync();

      assertThat(result.inserido()).isEqualTo(0);
      assertThat(result.inativado()).isEqualTo(0);
      assertThat(result.versionado()).isEqualTo(0);

      verify(repo, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("deve lidar com lista externa nula")
    void shouldHandleNullExternalList() {
      when(client.fetchAll()).thenReturn(null);
      when(repo.findByAtivoTrue()).thenReturn(List.of());

      SyncResult result = service.sync();

      assertThat(result.inserido()).isEqualTo(0);
      assertThat(result.inativado()).isEqualTo(0);
      assertThat(result.versionado()).isEqualTo(0);
      assertThat(result.contagemExterna()).isEqualTo(0);
    }

    @Test
    @DisplayName("deve ignorar registros externos com id nulo")
    void shouldIgnoreExternalRecordsWithNullId() {
      RegionalExternalDto validExt = new RegionalExternalDto(1, "Regional Norte");
      RegionalExternalDto invalidExt = new RegionalExternalDto(null, "Regional Sem ID");

      when(client.fetchAll()).thenReturn(List.of(validExt, invalidExt));
      when(repo.findByAtivoTrue()).thenReturn(List.of());

      SyncResult result = service.sync();

      assertThat(result.inserido()).isEqualTo(1);
      assertThat(result.contagemExterna()).isEqualTo(1);
    }

    @Test
    @DisplayName("deve ignorar registros externos nulos")
    void shouldIgnoreNullExternalRecords() {
      List<RegionalExternalDto> externals = new ArrayList<>();
      externals.add(new RegionalExternalDto(1, "Regional Norte"));
      externals.add(null);

      when(client.fetchAll()).thenReturn(externals);
      when(repo.findByAtivoTrue()).thenReturn(List.of());

      SyncResult result = service.sync();

      assertThat(result.inserido()).isEqualTo(1);
      assertThat(result.contagemExterna()).isEqualTo(1);
    }

    @Test
    @DisplayName("deve comparar nomes com trim")
    void shouldCompareNamesWithTrim() {
      RegionalEntity existing = new RegionalEntity();
      existing.setId(1L);
      existing.setExternalId(1);
      existing.setNome("Regional Norte");
      existing.setAtivo(true);

      RegionalExternalDto ext = new RegionalExternalDto(1, "  Regional Norte  ");

      when(client.fetchAll()).thenReturn(List.of(ext));
      when(repo.findByAtivoTrue()).thenReturn(List.of(existing));

      SyncResult result = service.sync();

      assertThat(result.versionado()).isEqualTo(0);
    }

    @Test
    @DisplayName("deve lidar com cenario complexo")
    void shouldHandleComplexScenario() {
      RegionalEntity existingUnchanged = new RegionalEntity();
      existingUnchanged.setId(1L);
      existingUnchanged.setExternalId(1);
      existingUnchanged.setNome("Regional Norte");
      existingUnchanged.setAtivo(true);

      RegionalEntity existingToVersion = new RegionalEntity();
      existingToVersion.setId(2L);
      existingToVersion.setExternalId(2);
      existingToVersion.setNome("Nome Antigo");
      existingToVersion.setAtivo(true);

      RegionalEntity existingToInactivate = new RegionalEntity();
      existingToInactivate.setId(3L);
      existingToInactivate.setExternalId(3);
      existingToInactivate.setNome("Regional a Inativar");
      existingToInactivate.setAtivo(true);

      RegionalExternalDto extUnchanged = new RegionalExternalDto(1, "Regional Norte");
      RegionalExternalDto extToVersion = new RegionalExternalDto(2, "Nome Novo");
      RegionalExternalDto extNew = new RegionalExternalDto(4, "Nova Regional");

      when(client.fetchAll()).thenReturn(List.of(extUnchanged, extToVersion, extNew));
      when(repo.findByAtivoTrue()).thenReturn(
          List.of(existingUnchanged, existingToVersion, existingToInactivate));

      SyncResult result = service.sync();

      assertThat(result.inserido()).isEqualTo(1);
      assertThat(result.inativado()).isEqualTo(2);
      assertThat(result.versionado()).isEqualTo(1);
      assertThat(result.contagemExterna()).isEqualTo(3);
    }
  }
}
