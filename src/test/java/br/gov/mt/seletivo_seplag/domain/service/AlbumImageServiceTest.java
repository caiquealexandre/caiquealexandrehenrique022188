package br.gov.mt.seletivo_seplag.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.gov.mt.seletivo_seplag.domain.exception.NotFoundException;
import br.gov.mt.seletivo_seplag.infra.persistence.entity.AlbumEntity;
import br.gov.mt.seletivo_seplag.infra.persistence.entity.AlbumImageEntity;
import br.gov.mt.seletivo_seplag.infra.persistence.repository.AlbumImageRepository;
import br.gov.mt.seletivo_seplag.infra.persistence.repository.AlbumRepository;
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
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class AlbumImageServiceTest {

  @Mock
  private AlbumRepository albumRepo;

  @Mock
  private AlbumImageRepository imgRepo;

  @Mock
  private MinioStorageService storage;

  @Mock
  private MultipartFile file1;

  @Mock
  private MultipartFile file2;

  @InjectMocks
  private AlbumImageService service;

  @Nested
  @DisplayName("upload()")
  class UploadTests {

    @Test
    @DisplayName("deve fazer upload de multiplos arquivos")
    void shouldUploadMultipleFiles() {
      AlbumEntity album = new AlbumEntity();
      album.setId(1L);

      when(albumRepo.findById(1L)).thenReturn(Optional.of(album));
      when(storage.upload(eq(1L), any(MultipartFile.class)))
          .thenReturn("albums/1/uuid1.jpg")
          .thenReturn("albums/1/uuid2.png");
      when(file1.getContentType()).thenReturn("image/jpeg");
      when(file1.getSize()).thenReturn(1024L);
      when(file2.getContentType()).thenReturn("image/png");
      when(file2.getSize()).thenReturn(2048L);

      service.upload(1L, List.of(file1, file2));

      verify(storage, times(2)).upload(eq(1L), any(MultipartFile.class));
      verify(imgRepo, times(2)).save(any(AlbumImageEntity.class));

      ArgumentCaptor<AlbumImageEntity> captor = ArgumentCaptor.forClass(AlbumImageEntity.class);
      verify(imgRepo, times(2)).save(captor.capture());

      List<AlbumImageEntity> saved = captor.getAllValues();
      assertThat(saved).hasSize(2);
      assertThat(saved.get(0).getObjectKey()).isEqualTo("albums/1/uuid1.jpg");
      assertThat(saved.get(0).getContentType()).isEqualTo("image/jpeg");
      assertThat(saved.get(0).getSizeBytes()).isEqualTo(1024L);
      assertThat(saved.get(1).getObjectKey()).isEqualTo("albums/1/uuid2.png");
    }

    @Test
    @DisplayName("deve lancar NotFoundException quando album nao existe")
    void shouldThrowNotFoundWhenAlbumDoesNotExist() {
      when(albumRepo.findById(999L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> service.upload(999L, List.of(file1)))
          .isInstanceOf(NotFoundException.class)
          .hasMessageContaining("999");
    }

    @Test
    @DisplayName("deve fazer upload de arquivo unico")
    void shouldUploadSingleFile() {
      AlbumEntity album = new AlbumEntity();
      album.setId(1L);

      when(albumRepo.findById(1L)).thenReturn(Optional.of(album));
      when(storage.upload(1L, file1)).thenReturn("albums/1/image.jpg");
      when(file1.getContentType()).thenReturn("image/jpeg");
      when(file1.getSize()).thenReturn(512L);

      service.upload(1L, List.of(file1));

      verify(storage).upload(1L, file1);
      verify(imgRepo).save(any(AlbumImageEntity.class));
    }
  }

  @Nested
  @DisplayName("list()")
  class ListTests {

    @Test
    @DisplayName("deve listar imagens de um album")
    void shouldListAlbumImages() {
      AlbumImageEntity img1 = new AlbumImageEntity();
      img1.setId(1L);
      img1.setObjectKey("albums/1/img1.jpg");

      AlbumImageEntity img2 = new AlbumImageEntity();
      img2.setId(2L);
      img2.setObjectKey("albums/1/img2.jpg");

      when(imgRepo.findByAlbumId(1L)).thenReturn(List.of(img1, img2));

      List<AlbumImageEntity> result = service.list(1L);

      assertThat(result).hasSize(2);
      assertThat(result.get(0).getObjectKey()).isEqualTo("albums/1/img1.jpg");
      assertThat(result.get(1).getObjectKey()).isEqualTo("albums/1/img2.jpg");
    }

    @Test
    @DisplayName("deve retornar lista vazia quando album nao tem imagens")
    void shouldReturnEmptyListWhenNoImages() {
      when(imgRepo.findByAlbumId(1L)).thenReturn(List.of());

      List<AlbumImageEntity> result = service.list(1L);

      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("presigned()")
  class PresignedTests {

    @Test
    @DisplayName("deve retornar URL presignada")
    void shouldReturnPresignedUrl() {
      String objectKey = "albums/1/image.jpg";
      String expectedUrl = "https://minio.example.com/bucket/albums/1/image.jpg?signature=xxx";

      when(storage.getUrl(objectKey)).thenReturn(expectedUrl);

      String result = service.presigned(objectKey);

      assertThat(result).isEqualTo(expectedUrl);
      verify(storage).getUrl(objectKey);
    }
  }
}
