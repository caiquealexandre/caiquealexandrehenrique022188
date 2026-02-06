package br.gov.mt.seletivo_seplag.domain.service;

import br.gov.mt.seletivo_seplag.domain.exception.NotFoundException;
import br.gov.mt.seletivo_seplag.infra.persistence.entity.AlbumEntity;
import br.gov.mt.seletivo_seplag.infra.persistence.entity.ArtistaEntity;
import br.gov.mt.seletivo_seplag.infra.persistence.repository.AlbumRepository;
import br.gov.mt.seletivo_seplag.infra.persistence.repository.ArtistaRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AlbumService {

  private final AlbumRepository albumRepo;
  private final ArtistaRepository artistaRepo;
  private final SimpMessagingTemplate messaging;

  @Transactional
  public AlbumEntity create(String title, Integer releaseYear, List<Long> artistIds) {
    Set<ArtistaEntity> artistas = new HashSet<>(artistaRepo.findAllById(artistIds));
    if (artistas.size() != artistIds.size()) {
      throw new NotFoundException("Um ou mais artistas não encontrados!");
    }

    AlbumEntity album = new AlbumEntity();
    album.setTitulo(title);
    album.setAnoLancamento(releaseYear);
    album.getArtistas().addAll(artistas);

    AlbumEntity saved = albumRepo.save(album);

    // Envio de notificacao atraves do WebSocket (Para notificar no web/mobile ou outro consumidor do back-end)
    messaging.convertAndSend("/topic/albums",
        Optional.of(Map.of("event", "ALBUM_CREATED", "albumId", saved.getId(), "titulo",
            saved.getTitulo())));

    return saved;
  }

  @Transactional
  public AlbumEntity update(Long id, String titulo, Integer anoLancamento, List<Long> artistaIds) {
    AlbumEntity album = albumRepo.findById(id)
        .orElseThrow(() -> new NotFoundException("Album not found: " + id));
    Set<ArtistaEntity> artistas = new HashSet<>(artistaRepo.findAllById(artistaIds));
    if (artistas.size() != artistaIds.size()) {
      throw new NotFoundException("One or more artists not found");
    }

    album.setTitulo(titulo);
    album.setAnoLancamento(anoLancamento);
    album.getArtistas().clear();
    album.getArtistas().addAll(artistas);

    return albumRepo.save(album);
  }

  public AlbumEntity get(Long id) {
    return albumRepo.findById(id)
        .orElseThrow(() -> new NotFoundException("Album not found: " + id));
  }

  public Page<AlbumEntity> list(String artistName, Pageable pageable) {
    return albumRepo.findAllFiltered(artistName, pageable);
  }
}
