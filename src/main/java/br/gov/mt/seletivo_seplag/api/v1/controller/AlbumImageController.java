package br.gov.mt.seletivo_seplag.api.v1.controller;

import br.gov.mt.seletivo_seplag.api.v1.dto.AlbumImageDtos;
import br.gov.mt.seletivo_seplag.domain.service.AlbumImageService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/albums/{albumId}/imagens")
@RequiredArgsConstructor
public class AlbumImageController {

  private final AlbumImageService service;

  @PostMapping(consumes = "multipart/form-data")
  public void upload(@PathVariable Long albumId, @RequestPart("files") List<MultipartFile> files) {
    service.upload(albumId, files);
  }

  @GetMapping
  public List<AlbumImageDtos.Response> list(@PathVariable Long albumId) {
    return service.list(albumId).stream()
        .map(img -> new AlbumImageDtos.Response(img.getId(), img.getObjectKey(),
            service.presigned(img.getObjectKey())))
        .toList();
  }
}
