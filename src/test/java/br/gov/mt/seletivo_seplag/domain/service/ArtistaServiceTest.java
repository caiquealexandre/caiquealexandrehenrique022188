package br.gov.mt.seletivo_seplag.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.gov.mt.seletivo_seplag.domain.exception.NotFoundException;
import br.gov.mt.seletivo_seplag.infra.persistence.entity.ArtistaEntity;
import br.gov.mt.seletivo_seplag.infra.persistence.entity.enums.TipoArtistaEnum;
import br.gov.mt.seletivo_seplag.infra.persistence.repository.ArtistaRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class ArtistaServiceTest {

  @Mock
  private ArtistaRepository repo;

  @InjectMocks
  private ArtistaService service;

  @Nested
  @DisplayName("create()")
  class CreateTests {

    @Test
    @DisplayName("deve criar artista com sucesso")
    void shouldCreateArtist() {
      ArtistaEntity saved = new ArtistaEntity();
      saved.setId(1L);
      saved.setNome("Serj Tankian");
      saved.setTipoArtista(TipoArtistaEnum.SINGER);

      when(repo.save(any(ArtistaEntity.class))).thenReturn(saved);

      ArtistaEntity result = service.create("Serj Tankian", TipoArtistaEnum.SINGER);

      assertThat(result.getId()).isEqualTo(1L);
      assertThat(result.getNome()).isEqualTo("Serj Tankian");
      assertThat(result.getTipoArtista()).isEqualTo(TipoArtistaEnum.SINGER);

      ArgumentCaptor<ArtistaEntity> captor = ArgumentCaptor.forClass(ArtistaEntity.class);
      verify(repo).save(captor.capture());
      assertThat(captor.getValue().getNome()).isEqualTo("Serj Tankian");
      assertThat(captor.getValue().getTipoArtista()).isEqualTo(TipoArtistaEnum.SINGER);
    }

    @Test
    @DisplayName("deve criar artista do tipo BAND")
    void shouldCreateBandArtist() {
      ArtistaEntity saved = new ArtistaEntity();
      saved.setId(2L);
      saved.setNome("Guns N' Roses");
      saved.setTipoArtista(TipoArtistaEnum.BAND);

      when(repo.save(any(ArtistaEntity.class))).thenReturn(saved);

      ArtistaEntity result = service.create("Guns N' Roses", TipoArtistaEnum.BAND);

      assertThat(result.getTipoArtista()).isEqualTo(TipoArtistaEnum.BAND);
    }
  }

  @Nested
  @DisplayName("update()")
  class UpdateTests {

    @Test
    @DisplayName("deve atualizar artista existente")
    void shouldUpdateExistingArtist() {
      ArtistaEntity existing = new ArtistaEntity();
      existing.setId(1L);
      existing.setNome("Nome Antigo");
      existing.setTipoArtista(TipoArtistaEnum.SINGER);

      ArtistaEntity updated = new ArtistaEntity();
      updated.setId(1L);
      updated.setNome("Nome Novo");
      updated.setTipoArtista(TipoArtistaEnum.BAND);

      when(repo.findById(1L)).thenReturn(Optional.of(existing));
      when(repo.save(any(ArtistaEntity.class))).thenReturn(updated);

      ArtistaEntity result = service.update(1L, "Nome Novo", TipoArtistaEnum.BAND);

      assertThat(result.getNome()).isEqualTo("Nome Novo");
      assertThat(result.getTipoArtista()).isEqualTo(TipoArtistaEnum.BAND);
      verify(repo).findById(1L);
      verify(repo).save(existing);
    }

    @Test
    @DisplayName("deve lancar NotFoundException quando artista nao existe")
    void shouldThrowNotFoundWhenArtistDoesNotExist() {
      when(repo.findById(999L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> service.update(999L, "Nome", TipoArtistaEnum.SINGER))
          .isInstanceOf(NotFoundException.class)
          .hasMessageContaining("999");
    }
  }

  @Nested
  @DisplayName("get()")
  class GetTests {

    @Test
    @DisplayName("deve retornar artista quando existe")
    void shouldReturnArtistWhenExists() {
      ArtistaEntity entity = new ArtistaEntity();
      entity.setId(1L);
      entity.setNome("Mike Shinoda");

      when(repo.findById(1L)).thenReturn(Optional.of(entity));

      ArtistaEntity result = service.get(1L);

      assertThat(result.getId()).isEqualTo(1L);
      assertThat(result.getNome()).isEqualTo("Mike Shinoda");
    }

    @Test
    @DisplayName("deve lancar NotFoundException quando artista nao existe")
    void shouldThrowNotFoundWhenArtistDoesNotExist() {
      when(repo.findById(999L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> service.get(999L))
          .isInstanceOf(NotFoundException.class)
          .hasMessageContaining("999");
    }
  }

  @Nested
  @DisplayName("search()")
  class SearchTests {

    private Pageable pageable;

    @BeforeEach
    void setUp() {
      pageable = PageRequest.of(0, 10);
    }

    @Test
    @DisplayName("deve buscar por nome quando tipo eh nulo")
    void shouldSearchByNameWhenTypeIsNull() {
      ArtistaEntity artista = new ArtistaEntity();
      artista.setId(1L);
      artista.setNome("Serj");
      Page<ArtistaEntity> page = new PageImpl<>(List.of(artista));

      when(repo.findByNomeContainingIgnoreCase("Serj", pageable)).thenReturn(page);

      Page<ArtistaEntity> result = service.search("Serj", null, pageable);

      assertThat(result.getContent()).hasSize(1);
      verify(repo).findByNomeContainingIgnoreCase("Serj", pageable);
    }

    @Test
    @DisplayName("deve buscar por nome e tipo quando tipo nao eh nulo")
    void shouldSearchByNameAndTypeWhenTypeIsNotNull() {
      ArtistaEntity artista = new ArtistaEntity();
      artista.setId(1L);
      artista.setNome("Serj");
      artista.setTipoArtista(TipoArtistaEnum.SINGER);
      Page<ArtistaEntity> page = new PageImpl<>(List.of(artista));

      when(repo.findByNomeContainingIgnoreCaseAndTipoArtista("Serj", TipoArtistaEnum.SINGER, pageable))
          .thenReturn(page);

      Page<ArtistaEntity> result = service.search("Serj", TipoArtistaEnum.SINGER, pageable);

      assertThat(result.getContent()).hasSize(1);
      verify(repo).findByNomeContainingIgnoreCaseAndTipoArtista("Serj", TipoArtistaEnum.SINGER, pageable);
    }

    @Test
    @DisplayName("deve usar string vazia quando nome eh nulo")
    void shouldUseEmptyStringWhenNameIsNull() {
      Page<ArtistaEntity> page = new PageImpl<>(List.of());

      when(repo.findByNomeContainingIgnoreCase("", pageable)).thenReturn(page);

      service.search(null, null, pageable);

      verify(repo).findByNomeContainingIgnoreCase("", pageable);
    }
  }
}
