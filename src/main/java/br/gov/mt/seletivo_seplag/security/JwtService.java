package br.gov.mt.seletivo_seplag.security;

import br.gov.mt.seletivo_seplag.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtService {

  private final AppProperties props;

  public String generateAccessToken(String usuario, Set<String> perfis) {
    Instant now = Instant.now();
    Instant exp = now.plusSeconds(props.security().jwt().accessTokenMinutes() * 60L);

    return Jwts.builder()
        .issuer(props.security().jwt().issuer())
        .subject(usuario)
        .claim("perfis", perfis)
        .issuedAt(Date.from(now))
        .expiration(Date.from(exp))
        .signWith(
            Keys.hmacShaKeyFor(props.security().jwt().secret().getBytes(StandardCharsets.UTF_8)))
        .compact();
  }

  public String generateRefreshToken(String usuario) {
    Instant now = Instant.now();
    Instant exp = now.plusSeconds(props.security().jwt().refreshTokenDays() * 86400L);

    return Jwts.builder()
        .issuer(props.security().jwt().issuer())
        .subject(usuario)
        .claim("typ", "refresh")
        .issuedAt(Date.from(now))
        .expiration(Date.from(exp))
        .signWith(
            Keys.hmacShaKeyFor(props.security().jwt().secret().getBytes(StandardCharsets.UTF_8)))
        .compact();
  }

  public Jws<Claims> parse(String token) {
    return Jwts.parser()
        .verifyWith(
            Keys.hmacShaKeyFor(props.security().jwt().secret().getBytes(StandardCharsets.UTF_8)))
        .build()
        .parseSignedClaims(token);
  }

  public boolean isRefreshToken(Jws<Claims> jws) {
    return "refresh".equals(jws.getPayload().get("typ", String.class));
  }
}
