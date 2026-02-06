package br.gov.mt.seletivo_seplag.security;

import br.gov.mt.seletivo_seplag.config.AppProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http, AppProperties props, JwtService jwt)
      throws Exception {

    JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwt);
    RateLimitFilter rateLimitFilter = new RateLimitFilter(props);
    OriginBlockFilter originBlockFilter = new OriginBlockFilter(props);

    http
        .csrf(csrf -> csrf.disable())
        .cors(Customizer.withDefaults())
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(
                "/api/v1/auth/**",
                "/actuator/**",
                "/swagger-ui/**",
                "/v3/api-docs/**",
                "/ws/**"
            ).permitAll()
            .anyRequest().authenticated()
        );

    // Ordem ---> bloqueio origin -> jwt -> rate limit
    http.addFilterBefore(originBlockFilter, UsernamePasswordAuthenticationFilter.class);
    http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
    http.addFilterAfter(rateLimitFilter, JwtAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
