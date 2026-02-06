package br.gov.mt.seletivo_seplag.api.v1.dto;

public class RegionalDtos {

  public record Response(
      Long id,
      Integer externalId,
      String nome,
      Boolean ativo
  ) {}

}
