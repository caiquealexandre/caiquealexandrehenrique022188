package br.gov.mt.seletivo_seplag.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.gov.mt.seletivo_seplag.config.AppProperties;
import br.gov.mt.seletivo_seplag.config.AppProperties.Minio;
import br.gov.mt.seletivo_seplag.domain.exception.BadRequestException;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.junit.jupiter.api.BeforeEach;
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
class MinioStorageServiceTest {

  @Mock
  private MinioClient minio;

  @Mock
  private AppProperties props;

  @Mock
  private Minio minioProps;

  @Mock
  private MultipartFile file;

  @InjectMocks
  private MinioStorageService service;

  @Nested
  @DisplayName("upload()")
  class UploadTests {

    @BeforeEach
    void setUp() {
      when(props.minio()).thenReturn(minioProps);
      when(minioProps.bucket()).thenReturn("album-images");
    }

    @Test
    @DisplayName("deve fazer upload com sucesso")
    void shouldUploadSuccessfully() throws Exception {
      InputStream inputStream = new ByteArrayInputStream("test content".getBytes());

      when(file.getOriginalFilename()).thenReturn("image.jpg");
      when(file.getInputStream()).thenReturn(inputStream);
      when(file.getSize()).thenReturn(12L);
      when(file.getContentType()).thenReturn("image/jpeg");

      String objectKey = service.upload(1L, file);

      assertThat(objectKey).startsWith("albums/1/");
      assertThat(objectKey).endsWith(".jpg");

      ArgumentCaptor<PutObjectArgs> captor = ArgumentCaptor.forClass(PutObjectArgs.class);
      verify(minio).putObject(captor.capture());

      PutObjectArgs args = captor.getValue();
      assertThat(args.bucket()).isEqualTo("album-images");
      assertThat(args.object()).startsWith("albums/1/");
    }

    @Test
    @DisplayName("deve fazer upload de arquivo sem extensao")
    void shouldUploadFileWithoutExtension() throws Exception {
      InputStream inputStream = new ByteArrayInputStream("test".getBytes());

      when(file.getOriginalFilename()).thenReturn("noextension");
      when(file.getInputStream()).thenReturn(inputStream);
      when(file.getSize()).thenReturn(4L);
      when(file.getContentType()).thenReturn("application/octet-stream");

      String objectKey = service.upload(1L, file);

      assertThat(objectKey).startsWith("albums/1/");
      assertThat(objectKey).doesNotContain(".");
    }

    @Test
    @DisplayName("deve fazer upload de arquivo com nome nulo")
    void shouldUploadFileWithNullFilename() throws Exception {
      InputStream inputStream = new ByteArrayInputStream("test".getBytes());

      when(file.getOriginalFilename()).thenReturn(null);
      when(file.getInputStream()).thenReturn(inputStream);
      when(file.getSize()).thenReturn(4L);
      when(file.getContentType()).thenReturn("application/octet-stream");

      String objectKey = service.upload(1L, file);

      assertThat(objectKey).startsWith("albums/1/");
    }

    @Test
    @DisplayName("deve ignorar extensao muito longa")
    void shouldIgnoreLongExtension() throws Exception {
      InputStream inputStream = new ByteArrayInputStream("test".getBytes());

      when(file.getOriginalFilename()).thenReturn("file.verylongextension");
      when(file.getInputStream()).thenReturn(inputStream);
      when(file.getSize()).thenReturn(4L);
      when(file.getContentType()).thenReturn("application/octet-stream");

      String objectKey = service.upload(1L, file);

      assertThat(objectKey).doesNotContain(".verylongextension");
    }

    @Test
    @DisplayName("deve lancar BadRequestException quando MinIO falha")
    void shouldThrowWhenMinioFails() throws Exception {
      when(file.getOriginalFilename()).thenReturn("image.jpg");
      when(file.getInputStream()).thenThrow(new RuntimeException("Connection failed"));

      assertThatThrownBy(() -> service.upload(1L, file))
          .isInstanceOf(BadRequestException.class)
          .hasMessageContaining("MinIO");
    }
  }

  @Nested
  @DisplayName("getUrl()")
  class GetUrlTests {

    @BeforeEach
    void setUp() {
      when(props.minio()).thenReturn(minioProps);
      when(minioProps.bucket()).thenReturn("album-images");
      when(minioProps.presignExpMinutes()).thenReturn(30);
    }

    @Test
    @DisplayName("deve retornar URL presignada")
    void shouldReturnPresignedUrl() throws Exception {
      String expectedUrl = "https://minio.example.com/album-images/albums/1/img.jpg?signature=xxx";

      when(minio.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
          .thenReturn(expectedUrl);

      String result = service.getUrl("albums/1/img.jpg");

      assertThat(result).isEqualTo(expectedUrl);

      ArgumentCaptor<GetPresignedObjectUrlArgs> captor =
          ArgumentCaptor.forClass(GetPresignedObjectUrlArgs.class);
      verify(minio).getPresignedObjectUrl(captor.capture());

      GetPresignedObjectUrlArgs args = captor.getValue();
      assertThat(args.bucket()).isEqualTo("album-images");
      assertThat(args.object()).isEqualTo("albums/1/img.jpg");
      assertThat(args.expiry()).isEqualTo(30 * 60);
    }

    @Test
    @DisplayName("deve lancar BadRequestException quando MinIO falha")
    void shouldThrowWhenMinioFails() throws Exception {
      when(minio.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
          .thenThrow(new RuntimeException("Connection failed"));

      assertThatThrownBy(() -> service.getUrl("albums/1/img.jpg"))
          .isInstanceOf(BadRequestException.class)
          .hasMessageContaining("MinIO");
    }
  }
}
