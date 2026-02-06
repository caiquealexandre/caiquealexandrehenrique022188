package br.gov.mt.seletivo_seplag.api.v1.controller;

import br.gov.mt.seletivo_seplag.api.v1.dto.ArtistaDtos;
import br.gov.mt.seletivo_seplag.api.v1.dto.ArtistaDtos.Response;
import br.gov.mt.seletivo_seplag.domain.service.ArtistaService;
import br.gov.mt.seletivo_seplag.infra.persistence.entity.ArtistaEntity;
import br.gov.mt.seletivo_seplag.infra.persistence.entity.enums.TipoArtistaEnum;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/artistas")
@RequiredArgsConstructor
public class ArtistaController {

  private final ArtistaService service;

  @PostMapping
  public ArtistaDtos.Response create(@Valid @RequestBody ArtistaDtos.CreateRequest req) {
    ArtistaEntity artista = service.create(req.nome(), req.tipoArtistaEnum());
    return new ArtistaDtos.Response(artista.getId(), artista.getNome(), artista.getTipoArtista());
  }

  @PutMapping("/{id}")
  public ArtistaDtos.Response update(@PathVariable Long id,
      @Valid @RequestBody ArtistaDtos.UpdateRequest req) {
    ArtistaEntity e = service.update(id, req.nome(), req.tipoArtistaEnum());
    return new ArtistaDtos.Response(e.getId(), e.getNome(), e.getTipoArtista());
  }

  @GetMapping("/{id}")
  public ArtistaDtos.Response get(@PathVariable Long id) {
    ArtistaEntity e = service.get(id);
    return new ArtistaDtos.Response(e.getId(), e.getNome(), e.getTipoArtista());
  }

  @GetMapping
  public Page<Response> search(
      @RequestParam(required = false) String nome,
      @RequestParam(required = false) TipoArtistaEnum tipoArtistaEnum,
      Pageable pageable
  ) {
    return service.search(nome, tipoArtistaEnum, pageable)
        .map(e -> new ArtistaDtos.Response(e.getId(), e.getNome(), e.getTipoArtista()));
  }
}
