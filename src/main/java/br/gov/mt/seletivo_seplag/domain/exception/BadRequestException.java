package br.gov.mt.seletivo_seplag.domain.exception;

public class BadRequestException extends RuntimeException {

  public BadRequestException(String message) {
    super(message);
  }
}
