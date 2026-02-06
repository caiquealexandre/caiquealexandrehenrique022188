package br.gov.mt.seletivo_seplag.domain.service;

import br.gov.mt.seletivo_seplag.config.AppProperties;
import br.gov.mt.seletivo_seplag.domain.exception.BadRequestException;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import java.io.InputStream;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class MinioStorageService {

  private final MinioClient minio;
  private final AppProperties props;

  public String upload(Long albumId, MultipartFile file) {
    try {
      String bucket = props.minio().bucket();
      String ext = safeExt(file.getOriginalFilename());
      String objectKey = "albums/" + albumId + "/" + UUID.randomUUID() + ext;

      try (InputStream in = file.getInputStream()) {
        minio.putObject(
            PutObjectArgs.builder()
                .bucket(bucket)
                .object(objectKey)
                .stream(in, file.getSize(), -1)
                .contentType(file.getContentType())
                .build()
        );
      }
      return objectKey;
    } catch (Exception e) {
      throw new BadRequestException("Falha ao enviar o arquivo para o MinIO: " + e.getMessage());
    }
  }

  public String getUrl(String objectKey) {
    try {
      return minio.getPresignedObjectUrl(
          GetPresignedObjectUrlArgs.builder()
              .method(Method.GET)
              .bucket(props.minio().bucket())
              .object(objectKey)
              .expiry(props.minio().presignExpMinutes() * 60)
              .build()
      );
    } catch (Exception e) {
      throw new BadRequestException(
          "Falha ao gerar URL de obtenção do arquivo no MinIO: " + e.getMessage());
    }
  }

  private String safeExt(String filename) {
    if (filename == null) {
      return "";
    }
    int idx = filename.lastIndexOf('.');
    if (idx < 0) {
      return "";
    }
    String ext = filename.substring(idx);
    if (ext.length() > 10) {
      return "";
    }
    return ext;
  }
}
