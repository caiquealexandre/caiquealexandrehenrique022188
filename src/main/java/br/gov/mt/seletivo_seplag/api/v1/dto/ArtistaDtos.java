package br.gov.mt.seletivo_seplag.api.v1.dto;

import br.gov.mt.seletivo_seplag.infra.persistence.entity.enums.TipoArtistaEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ArtistaDtos {

  public record CreateRequest(
      @NotBlank String nome,
      @NotNull TipoArtistaEnum tipoArtistaEnum
  ) { }

  public record UpdateRequest(
      @NotBlank String nome,
      @NotNull TipoArtistaEnum tipoArtistaEnum
  ) { }

  public record Response(
      Long id,
      String nome,
      TipoArtistaEnum tipoArtistaEnum
  ) { }

}
