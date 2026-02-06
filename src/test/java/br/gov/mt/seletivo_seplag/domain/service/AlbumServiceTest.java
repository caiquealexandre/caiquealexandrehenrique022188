package br.gov.mt.seletivo_seplag.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.gov.mt.seletivo_seplag.domain.exception.NotFoundException;
import br.gov.mt.seletivo_seplag.infra.persistence.entity.AlbumEntity;
import br.gov.mt.seletivo_seplag.infra.persistence.entity.ArtistaEntity;
import br.gov.mt.seletivo_seplag.infra.persistence.repository.AlbumRepository;
import br.gov.mt.seletivo_seplag.infra.persistence.repository.ArtistaRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
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
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
class AlbumServiceTest {

  @Mock
  private AlbumRepository albumRepo;

  @Mock
  private ArtistaRepository artistaRepo;

  @Mock
  private SimpMessagingTemplate messaging;

  @InjectMocks
  private AlbumService service;

  @Nested
  @DisplayName("create()")
  class CreateTests {

    @Test
    @DisplayName("deve criar album com sucesso")
    void shouldCreateAlbum() {
      ArtistaEntity artista1 = new ArtistaEntity();
      artista1.setId(1L);
      artista1.setNome("Artista 1");

      ArtistaEntity artista2 = new ArtistaEntity();
      artista2.setId(2L);
      artista2.setNome("Artista 2");

      when(artistaRepo.findAllById(List.of(1L, 2L))).thenReturn(List.of(artista1, artista2));

      AlbumEntity saved = new AlbumEntity();
      saved.setId(1L);
      saved.setTitulo("Novo Album");
      saved.setAnoLancamento(2024);
      saved.setArtistas(new HashSet<>(List.of(artista1, artista2)));

      when(albumRepo.save(any(AlbumEntity.class))).thenReturn(saved);

      AlbumEntity result = service.create("Novo Album", 2024, List.of(1L, 2L));

      assertThat(result.getId()).isEqualTo(1L);
      assertThat(result.getTitulo()).isEqualTo("Novo Album");
      assertThat(result.getAnoLancamento()).isEqualTo(2024);

      verify(messaging).convertAndSend(eq("/topic/albums"), any(Optional.class));
    }

    @Test
    @DisplayName("deve lancar NotFoundException quando artista nao existe")
    void shouldThrowNotFoundWhenArtistDoesNotExist() {
      ArtistaEntity artista1 = new ArtistaEntity();
      artista1.setId(1L);

      when(artistaRepo.findAllById(List.of(1L, 2L, 3L))).thenReturn(List.of(artista1));

      assertThatThrownBy(() -> service.create("Album", 2024, List.of(1L, 2L, 3L)))
          .isInstanceOf(NotFoundException.class)
          .hasMessageContaining("artistas");
    }

    @Test
    @DisplayName("deve criar album sem ano de lancamento")
    void shouldCreateAlbumWithoutReleaseYear() {
      ArtistaEntity artista = new ArtistaEntity();
      artista.setId(1L);

      when(artistaRepo.findAllById(List.of(1L))).thenReturn(List.of(artista));

      AlbumEntity saved = new AlbumEntity();
      saved.setId(1L);
      saved.setTitulo("Album Sem Ano");
      saved.setAnoLancamento(null);

      when(albumRepo.save(any(AlbumEntity.class))).thenReturn(saved);

      AlbumEntity result = service.create("Album Sem Ano", null, List.of(1L));

      assertThat(result.getAnoLancamento()).isNull();
    }
  }

  @Nested
  @DisplayName("update()")
  class UpdateTests {

    @Test
    @DisplayName("deve atualizar album existente")
    void shouldUpdateExistingAlbum() {
      AlbumEntity existing = new AlbumEntity();
      existing.setId(1L);
      existing.setTitulo("Titulo Antigo");
      existing.setAnoLancamento(2020);
      existing.setArtistas(new HashSet<>());

      ArtistaEntity artista = new ArtistaEntity();
      artista.setId(1L);

      when(albumRepo.findById(1L)).thenReturn(Optional.of(existing));
      when(artistaRepo.findAllById(List.of(1L))).thenReturn(List.of(artista));

      AlbumEntity updated = new AlbumEntity();
      updated.setId(1L);
      updated.setTitulo("Titulo Novo");
      updated.setAnoLancamento(2024);

      when(albumRepo.save(any(AlbumEntity.class))).thenReturn(updated);

      AlbumEntity result = service.update(1L, "Titulo Novo", 2024, List.of(1L));

      assertThat(result.getTitulo()).isEqualTo("Titulo Novo");
      assertThat(result.getAnoLancamento()).isEqualTo(2024);
    }

    @Test
    @DisplayName("deve lancar NotFoundException quando album nao existe")
    void shouldThrowNotFoundWhenAlbumDoesNotExist() {
      when(albumRepo.findById(999L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> service.update(999L, "Titulo", 2024, List.of(1L)))
          .isInstanceOf(NotFoundException.class)
          .hasMessageContaining("999");
    }

    @Test
    @DisplayName("deve lancar NotFoundException quando artista nao existe na atualizacao")
    void shouldThrowNotFoundWhenArtistDoesNotExistOnUpdate() {
      AlbumEntity existing = new AlbumEntity();
      existing.setId(1L);
      existing.setArtistas(new HashSet<>());

      when(albumRepo.findById(1L)).thenReturn(Optional.of(existing));
      when(artistaRepo.findAllById(List.of(1L, 2L))).thenReturn(List.of());

      assertThatThrownBy(() -> service.update(1L, "Titulo", 2024, List.of(1L, 2L)))
          .isInstanceOf(NotFoundException.class)
          .hasMessageContaining("artists");
    }
  }

  @Nested
  @DisplayName("get()")
  class GetTests {

    @Test
    @DisplayName("deve retornar album quando existe")
    void shouldReturnAlbumWhenExists() {
      AlbumEntity entity = new AlbumEntity();
      entity.setId(1L);
      entity.setTitulo("Harakiri");

      when(albumRepo.findById(1L)).thenReturn(Optional.of(entity));

      AlbumEntity result = service.get(1L);

      assertThat(result.getId()).isEqualTo(1L);
      assertThat(result.getTitulo()).isEqualTo("Harakiri");
    }

    @Test
    @DisplayName("deve lancar NotFoundException quando album nao existe")
    void shouldThrowNotFoundWhenAlbumDoesNotExist() {
      when(albumRepo.findById(999L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> service.get(999L))
          .isInstanceOf(NotFoundException.class)
          .hasMessageContaining("999");
    }
  }

  @Nested
  @DisplayName("list()")
  class ListTests {

    @Test
    @DisplayName("deve listar albums filtrados por nome do artista")
    void shouldListAlbumsFilteredByArtistName() {
      Pageable pageable = PageRequest.of(0, 10);
      AlbumEntity album = new AlbumEntity();
      album.setId(1L);
      album.setTitulo("Album Test");
      Page<AlbumEntity> page = new PageImpl<>(List.of(album));

      when(albumRepo.findAllFiltered("Serj", pageable)).thenReturn(page);

      Page<AlbumEntity> result = service.list("Serj", pageable);

      assertThat(result.getContent()).hasSize(1);
      verify(albumRepo).findAllFiltered("Serj", pageable);
    }

    @Test
    @DisplayName("deve listar todos albums quando filtro eh nulo")
    void shouldListAllAlbumsWhenFilterIsNull() {
      Pageable pageable = PageRequest.of(0, 10);
      Page<AlbumEntity> page = new PageImpl<>(List.of());

      when(albumRepo.findAllFiltered(null, pageable)).thenReturn(page);

      service.list(null, pageable);

      verify(albumRepo).findAllFiltered(null, pageable);
    }
  }
}
