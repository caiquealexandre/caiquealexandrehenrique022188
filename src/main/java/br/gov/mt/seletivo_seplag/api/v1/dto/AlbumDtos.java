package br.gov.mt.seletivo_seplag.api.v1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class AlbumDtos {

  public record CreateRequest(
      @NotBlank String titulo,
      Integer anoLancamento,
      @NotEmpty List<Long> artistaIds
  ) { }

  public record UpdateRequest(
      @NotBlank String titulo,
      Integer anoLancamento,
      @NotEmpty List<Long> artistIds
  ) { }

  public record ArtistaMini(
      Long id,
      String nome
  ) { }

  public record Response(
      Long id,
      String titulo,
      Integer anoLancamento,
      List<ArtistaMini> artistas
  ) { }
}
