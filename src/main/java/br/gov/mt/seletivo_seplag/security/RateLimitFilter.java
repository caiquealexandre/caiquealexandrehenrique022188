package br.gov.mt.seletivo_seplag.security;

import br.gov.mt.seletivo_seplag.config.AppProperties;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class RateLimitFilter extends OncePerRequestFilter {

  private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();
  private final int capacity;
  private final Duration refillDuration;

  public RateLimitFilter(AppProperties props) {
    this.capacity = props.rateLimit().perUser().capacity();
    this.refillDuration = Duration.ofMinutes(props.rateLimit().perUser().refillMinutes());
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain)
      throws ServletException, IOException {

    String path = request.getRequestURI();
    if (path.startsWith("/api/v1/auth") || path.startsWith("/actuator") || path.startsWith(
        "/swagger")
        || path.startsWith("/v3/api-docs")) {
      chain.doFilter(request, response);
      return;
    }

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || auth.getName() == null) {
      chain.doFilter(request, response);
      return;
    }

    String key = auth.getName();
    Bucket bucket = buckets.computeIfAbsent(key, k -> Bucket.builder()
        .addLimit(Bandwidth.classic(capacity, Refill.intervally(capacity, refillDuration)))
        .build());

    if (bucket.tryConsume(1)) {
      chain.doFilter(request, response);
      return;
    }

    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    response.setContentType("application/json");
    response.getWriter().write("{\"message\":\"Limite de requisições excedido. Aguarde e tente novamente mais tarde!\"}");
  }
}
