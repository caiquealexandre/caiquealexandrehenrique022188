package br.gov.mt.seletivo_seplag.api.v1.dto;

public class AlbumImageDtos {

  public record Response(
      Long id,
      String objectKey,
      String downloadUrl
  ) {}

}
