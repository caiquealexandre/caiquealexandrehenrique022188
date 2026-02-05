package br.gov.mt.seletivo_seplag.security;

import br.gov.mt.seletivo_seplag.config.AppProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

public class OriginBlockFilter extends OncePerRequestFilter {

  private final Set<String> allowed;

  public OriginBlockFilter(AppProperties props) {
    this.allowed = new HashSet<>(props.api().allowedOrigins());
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain)
      throws ServletException, IOException {

    String origin = request.getHeader("Origin");
    if (origin != null && !allowed.contains(origin)) {
      response.setStatus(HttpStatus.FORBIDDEN.value());
      response.setContentType("application/json");
      response.getWriter().write("{\"message\":\"Origin not allowed\"}");
      return;
    }
    chain.doFilter(request, response);
  }
}
