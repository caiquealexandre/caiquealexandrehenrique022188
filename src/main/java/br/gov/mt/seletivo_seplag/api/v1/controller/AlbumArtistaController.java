package br.gov.mt.seletivo_seplag.api.v1.controller;

import br.gov.mt.seletivo_seplag.infra.persistence.entity.enums.TipoArtistaEnum;
import br.gov.mt.seletivo_seplag.infra.persistence.repository.AlbumRepository;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/albuns-artista")
@RequiredArgsConstructor
public class AlbumArtistaController {

  private final AlbumRepository albumRepo;

  @GetMapping("/albuns-por-tipo-artista")
  public List<Map<String, String>> albumsByArtistType(
      @RequestParam TipoArtistaEnum tipoArtistaEnum) {
    return albumRepo.findAll().stream()
        .filter(
            alb -> alb.getArtistas().stream().anyMatch(a -> a.getTipoArtista() == tipoArtistaEnum))
        .map(alb -> Map.of("albumId", alb.getId().toString(), "title", alb.getTitulo()))
        .toList();
  }
}
