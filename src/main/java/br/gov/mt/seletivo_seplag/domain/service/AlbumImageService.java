package br.gov.mt.seletivo_seplag.domain.service;

import br.gov.mt.seletivo_seplag.domain.exception.NotFoundException;
import br.gov.mt.seletivo_seplag.infra.persistence.entity.AlbumEntity;
import br.gov.mt.seletivo_seplag.infra.persistence.entity.AlbumImageEntity;
import br.gov.mt.seletivo_seplag.infra.persistence.repository.AlbumImageRepository;
import br.gov.mt.seletivo_seplag.infra.persistence.repository.AlbumRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AlbumImageService {

  private final AlbumRepository albumRepo;
  private final AlbumImageRepository imgRepo;
  private final MinioStorageService storage;

  @Transactional
  public void upload(Long albumId, List<MultipartFile> files) {
    AlbumEntity album = albumRepo.findById(albumId)
        .orElseThrow(() -> new NotFoundException("Album não encontrado: " + albumId));

    for (MultipartFile f : files) {
      String key = storage.upload(albumId, f);
      AlbumImageEntity img = new AlbumImageEntity();
      img.setAlbum(album);
      img.setObjectKey(key);
      img.setContentType(f.getContentType());
      img.setSizeBytes(f.getSize());
      imgRepo.save(img);
    }
  }

  @Transactional(readOnly = true)
  public List<AlbumImageEntity> list(Long albumId) {
    return imgRepo.findByAlbumId(albumId);
  }

  public String presigned(String objectKey) {
    return storage.getUrl(objectKey);
  }
}
