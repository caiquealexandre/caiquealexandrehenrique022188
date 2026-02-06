package br.gov.mt.seletivo_seplag.domain.service;

import br.gov.mt.seletivo_seplag.config.AppProperties;
import br.gov.mt.seletivo_seplag.domain.exception.BadRequestException;
import br.gov.mt.seletivo_seplag.infra.persistence.entity.RefreshTokenEntity;
import br.gov.mt.seletivo_seplag.infra.persistence.entity.UsuarioEntity;
import br.gov.mt.seletivo_seplag.infra.persistence.repository.RefreshTokenRepository;
import br.gov.mt.seletivo_seplag.infra.persistence.repository.UsuarioRepository;
import br.gov.mt.seletivo_seplag.security.JwtService;
import br.gov.mt.seletivo_seplag.security.TokenHasher;
import io.jsonwebtoken.Claims;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UsuarioRepository usuarioRepo;
  private final RefreshTokenRepository refreshRepo;
  private final PasswordEncoder passEncoder;
  private final JwtService jwt;
  private final AppProperties props;

  @Transactional
  public String[] login(String username, String password) {
    UsuarioEntity usuario = usuarioRepo.findByUsuario(username)
        .orElseThrow(() -> new BadRequestException("Invalid credentials"));

    if (!passEncoder.matches(password, usuario.getPassword())) {
      throw new BadRequestException("Credenciais inválidas");
    }

    Set<String> perfis = usuario.getPerfis().stream().map(Enum::name).collect(Collectors.toSet());
    String access = jwt.generateAccessToken(usuario.getUsuario(), perfis);
    String refresh = jwt.generateRefreshToken(usuario.getUsuario());

    saveAndRefresh(usuario, refresh);

    return new String[]{access, refresh};
  }

  @Transactional
  public String[] refresh(String refreshTokenRaw) {
    var jws = jwt.parse(refreshTokenRaw);
    Claims claims = jws.getPayload();
    if (!jwt.isRefreshToken(jws)) {
      throw new BadRequestException("Refresh token inválido");
    }

    String hash = TokenHasher.sha256Hex(refreshTokenRaw);

    RefreshTokenEntity stored = refreshRepo.findFirstByTokenHashAndRevogadoFalse(hash)
        .orElseThrow(() -> new BadRequestException("Refresh token não encontrado"));

    if (stored.getExpiresAt().isBefore(OffsetDateTime.now(ZoneOffset.UTC))) {
      stored.setRevogado(true);
      refreshRepo.save(stored);
      throw new BadRequestException("Refresh token expirado");
    }

    // Revoga o antigo e cria novo refresh
    stored.setRevogado(true);
    refreshRepo.save(stored);

    UsuarioEntity usuario = stored.getUsuario();
    Set<String> roles = usuario.getPerfis().stream().map(Enum::name).collect(Collectors.toSet());

    String newAccess = jwt.generateAccessToken(usuario.getUsuario(), roles);
    String newRefresh = jwt.generateRefreshToken(usuario.getUsuario());
    saveAndRefresh(usuario, newRefresh);

    return new String[]{newAccess, newRefresh};
  }

  private void saveAndRefresh(UsuarioEntity usuario, String refreshRaw) {
    OffsetDateTime exp = OffsetDateTime.now(ZoneOffset.UTC)
        .plusDays(props.security().jwt().refreshTokenDays());
    RefreshTokenEntity rt = new RefreshTokenEntity();
    rt.setUsuario(usuario);
    rt.setTokenHash(TokenHasher.sha256Hex(refreshRaw));
    rt.setExpiresAt(exp);
    rt.setRevogado(false);
    refreshRepo.save(rt);
  }
}
