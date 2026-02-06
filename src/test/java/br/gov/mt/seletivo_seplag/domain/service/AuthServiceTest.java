package br.gov.mt.seletivo_seplag.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.gov.mt.seletivo_seplag.config.AppProperties;
import br.gov.mt.seletivo_seplag.config.AppProperties.Security;
import br.gov.mt.seletivo_seplag.config.AppProperties.Security.Jwt;
import br.gov.mt.seletivo_seplag.domain.exception.BadRequestException;
import br.gov.mt.seletivo_seplag.infra.persistence.entity.RefreshTokenEntity;
import br.gov.mt.seletivo_seplag.infra.persistence.entity.UsuarioEntity;
import br.gov.mt.seletivo_seplag.infra.persistence.entity.enums.PerfilAcesso;
import br.gov.mt.seletivo_seplag.infra.persistence.repository.RefreshTokenRepository;
import br.gov.mt.seletivo_seplag.infra.persistence.repository.UsuarioRepository;
import br.gov.mt.seletivo_seplag.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock
  private UsuarioRepository usuarioRepo;

  @Mock
  private RefreshTokenRepository refreshRepo;

  @Mock
  private PasswordEncoder passEncoder;

  @Mock
  private JwtService jwt;

  @Mock
  private AppProperties props;

  @Mock
  private Security security;

  @Mock
  private Jwt jwtProps;

  @Mock
  private Jws<Claims> jws;

  @Mock
  private Claims claims;

  @InjectMocks
  private AuthService service;

  @Nested
  @DisplayName("login()")
  class LoginTests {

    private UsuarioEntity usuario;

    @BeforeEach
    void setUp() {
      usuario = new UsuarioEntity();
      usuario.setId(1L);
      usuario.setUsuario("admin");
      usuario.setPassword("hashedPassword");
      usuario.setPerfis(Set.of(PerfilAcesso.ADMIN));
    }

    @Test
    @DisplayName("deve realizar login com sucesso")
    void shouldLoginSuccessfully() {
      when(usuarioRepo.findByUsuario("admin")).thenReturn(Optional.of(usuario));
      when(passEncoder.matches("password123", "hashedPassword")).thenReturn(true);
      when(jwt.generateAccessToken(anyString(), anySet())).thenReturn("access-token");
      when(jwt.generateRefreshToken("admin")).thenReturn("refresh-token");
      when(props.security()).thenReturn(security);
      when(security.jwt()).thenReturn(jwtProps);
      when(jwtProps.refreshTokenDays()).thenReturn(7);

      String[] tokens = service.login("admin", "password123");

      assertThat(tokens).hasSize(2);
      assertThat(tokens[0]).isEqualTo("access-token");
      assertThat(tokens[1]).isEqualTo("refresh-token");
      verify(refreshRepo).save(any(RefreshTokenEntity.class));
    }

    @Test
    @DisplayName("deve lancar BadRequestException quando usuario nao existe")
    void shouldThrowWhenUserDoesNotExist() {
      when(usuarioRepo.findByUsuario("invalid")).thenReturn(Optional.empty());

      assertThatThrownBy(() -> service.login("invalid", "password"))
          .isInstanceOf(BadRequestException.class)
          .hasMessageContaining("Invalid credentials");
    }

    @Test
    @DisplayName("deve lancar BadRequestException quando senha esta incorreta")
    void shouldThrowWhenPasswordIsIncorrect() {
      when(usuarioRepo.findByUsuario("admin")).thenReturn(Optional.of(usuario));
      when(passEncoder.matches("wrongPassword", "hashedPassword")).thenReturn(false);

      assertThatThrownBy(() -> service.login("admin", "wrongPassword"))
          .isInstanceOf(BadRequestException.class)
          .hasMessageContaining("inválidas");
    }
  }

  @Nested
  @DisplayName("refresh()")
  class RefreshTests {

    private UsuarioEntity usuario;
    private RefreshTokenEntity storedToken;

    @BeforeEach
    void setUp() {
      usuario = new UsuarioEntity();
      usuario.setId(1L);
      usuario.setUsuario("admin");
      usuario.setPerfis(Set.of(PerfilAcesso.ADMIN));

      storedToken = new RefreshTokenEntity();
      storedToken.setId(1L);
      storedToken.setUsuario(usuario);
      storedToken.setRevogado(false);
      storedToken.setExpiresAt(OffsetDateTime.now(ZoneOffset.UTC).plusDays(1));
    }

    @Test
    @DisplayName("deve renovar tokens com sucesso")
    void shouldRefreshTokensSuccessfully() {
      String refreshTokenRaw = "valid-refresh-token";

      when(jwt.parse(refreshTokenRaw)).thenReturn(jws);
      when(jws.getPayload()).thenReturn(claims);
      when(jwt.isRefreshToken(jws)).thenReturn(true);
      when(refreshRepo.findFirstByTokenHashAndRevogadoFalse(anyString()))
          .thenReturn(Optional.of(storedToken));
      when(jwt.generateAccessToken(anyString(), anySet())).thenReturn("new-access-token");
      when(jwt.generateRefreshToken("admin")).thenReturn("new-refresh-token");
      when(props.security()).thenReturn(security);
      when(security.jwt()).thenReturn(jwtProps);
      when(jwtProps.refreshTokenDays()).thenReturn(7);

      String[] tokens = service.refresh(refreshTokenRaw);

      assertThat(tokens).hasSize(2);
      assertThat(tokens[0]).isEqualTo("new-access-token");
      assertThat(tokens[1]).isEqualTo("new-refresh-token");
      assertThat(storedToken.getRevogado()).isTrue();
      verify(refreshRepo, times(2)).save(any(RefreshTokenEntity.class));
    }

    @Test
    @DisplayName("deve lancar BadRequestException quando token nao eh refresh")
    void shouldThrowWhenTokenIsNotRefresh() {
      when(jwt.parse("access-token")).thenReturn(jws);
      when(jws.getPayload()).thenReturn(claims);
      when(jwt.isRefreshToken(jws)).thenReturn(false);

      assertThatThrownBy(() -> service.refresh("access-token"))
          .isInstanceOf(BadRequestException.class)
          .hasMessageContaining("inválido");
    }

    @Test
    @DisplayName("deve lancar BadRequestException quando token nao encontrado")
    void shouldThrowWhenTokenNotFound() {
      when(jwt.parse("unknown-token")).thenReturn(jws);
      when(jws.getPayload()).thenReturn(claims);
      when(jwt.isRefreshToken(jws)).thenReturn(true);
      when(refreshRepo.findFirstByTokenHashAndRevogadoFalse(anyString()))
          .thenReturn(Optional.empty());

      assertThatThrownBy(() -> service.refresh("unknown-token"))
          .isInstanceOf(BadRequestException.class)
          .hasMessageContaining("não encontrado");
    }

    @Test
    @DisplayName("deve lancar BadRequestException quando token expirado")
    void shouldThrowWhenTokenExpired() {
      storedToken.setExpiresAt(OffsetDateTime.now(ZoneOffset.UTC).minusDays(1));

      when(jwt.parse("expired-token")).thenReturn(jws);
      when(jws.getPayload()).thenReturn(claims);
      when(jwt.isRefreshToken(jws)).thenReturn(true);
      when(refreshRepo.findFirstByTokenHashAndRevogadoFalse(anyString()))
          .thenReturn(Optional.of(storedToken));

      assertThatThrownBy(() -> service.refresh("expired-token"))
          .isInstanceOf(BadRequestException.class)
          .hasMessageContaining("expirado");

      assertThat(storedToken.getRevogado()).isTrue();
      verify(refreshRepo).save(storedToken);
    }
  }
}
