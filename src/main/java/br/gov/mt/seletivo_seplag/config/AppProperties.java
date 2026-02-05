package br.gov.mt.seletivo_seplag.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
    Api api,
    Security security,
    Minio minio,
    RateLimit rateLimit
) {

  public record Api(List<String> allowedOrigins) {

  }

  public record Security(Jwt jwt) {

    public record Jwt(
        String issuer,
        String secret,
        int accessTokenMinutes,
        int refreshTokenDays
    ) {

    }
  }

  public record Minio(
      String endpoint,
      String accessKey,
      String secretKey,
      String bucket,
      int presignExpMinutes
  ) {

  }

  public record RateLimit(PerUser perUser) {

    public record PerUser(int capacity, int refillMinutes) {

    }
  }
}
