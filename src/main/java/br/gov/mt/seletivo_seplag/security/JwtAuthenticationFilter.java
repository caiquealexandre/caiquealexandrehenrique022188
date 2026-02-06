package br.gov.mt.seletivo_seplag.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwt;

  public JwtAuthenticationFilter(JwtService jwt) {
    this.jwt = jwt;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain)
      throws ServletException, IOException {

    String auth = request.getHeader("Authorization");
    if (auth == null || !auth.startsWith("Bearer ")) {
      chain.doFilter(request, response);
      return;
    }

    String token = auth.substring(7);
    try {
      Claims claims = jwt.parse(token).getPayload();
      String username = claims.getSubject();
      @SuppressWarnings("unchecked")
      List<String> roles = claims.get("roles", List.class);

      List<GrantedAuthority> authorities = new ArrayList<>();
      if (roles != null) {
        for (String r : roles) {
          authorities.add(new SimpleGrantedAuthority(r));
        }
      }

      Authentication authentication = new UsernamePasswordAuthenticationToken(username, null,
          authorities);
      SecurityContextHolder.getContext().setAuthentication(authentication);

    } catch (Exception ex) {
      // token inválido -> limpa contexto e segue (ou pode retornar 401)
      SecurityContextHolder.clearContext();
    }

    chain.doFilter(request, response);
  }
}
