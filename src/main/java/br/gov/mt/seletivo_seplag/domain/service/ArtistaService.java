package br.gov.mt.seletivo_seplag.domain.service;

import br.gov.mt.seletivo_seplag.domain.exception.NotFoundException;
import br.gov.mt.seletivo_seplag.infra.persistence.entity.ArtistaEntity;
import br.gov.mt.seletivo_seplag.infra.persistence.entity.enums.TipoArtistaEnum;
import br.gov.mt.seletivo_seplag.infra.persistence.repository.ArtistaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ArtistaService {

  private final ArtistaRepository repo;

  @Transactional
  public ArtistaEntity create(String nome, TipoArtistaEnum tipoArtistaEnum) {
    ArtistaEntity artista = new ArtistaEntity();
    artista.setNome(nome);
    artista.setTipoArtista(tipoArtistaEnum);
    return repo.save(artista);
  }

  @Transactional
  public ArtistaEntity update(Long id, String nome, TipoArtistaEnum tipoArtistaEnum) {
    ArtistaEntity e = repo.findById(id)
        .orElseThrow(() -> new NotFoundException("Artista não encontrado: " + id));
    e.setNome(nome);
    e.setTipoArtista(tipoArtistaEnum);
    return repo.save(e);
  }

  public ArtistaEntity get(Long id) {
    return repo.findById(id).orElseThrow(() -> new NotFoundException("Artista não encontrado: " + id));
  }

  public Page<ArtistaEntity> search(String name, TipoArtistaEnum tipoArtistaEnum, Pageable pageable) {
    String q = (name == null) ? "" : name;
    if (tipoArtistaEnum == null) {
      return repo.findByNomeContainingIgnoreCase(q, pageable);
    }
    return repo.findByNomeContainingIgnoreCaseAndTipoArtista(q, tipoArtistaEnum, pageable);
  }
}
