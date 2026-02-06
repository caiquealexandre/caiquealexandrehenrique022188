package br.gov.mt.seletivo_seplag.domain.exception;

public class NotFoundException extends RuntimeException {

  public NotFoundException(String message) {
    super(message);
  }
}
