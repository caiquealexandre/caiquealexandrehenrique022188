package br.gov.mt.seletivo_seplag.api.v1.controller;

import br.gov.mt.seletivo_seplag.api.v1.dto.AuthDtos;
import br.gov.mt.seletivo_seplag.domain.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService service;

  @PostMapping("/login")
  public AuthDtos.TokenResponse login(@Valid @RequestBody AuthDtos.LoginRequest req) {
    String[] tokens = service.login(req.username(), req.password());
    return new AuthDtos.TokenResponse(tokens[0], tokens[1]);
  }

  @PostMapping("/refresh")
  public AuthDtos.TokenResponse refresh(@Valid @RequestBody AuthDtos.RefreshRequest req) {
    String[] tokens = service.refresh(req.refreshToken());
    return new AuthDtos.TokenResponse(tokens[0], tokens[1]);
  }
}
