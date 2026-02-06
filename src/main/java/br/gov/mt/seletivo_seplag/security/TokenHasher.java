package br.gov.mt.seletivo_seplag.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class TokenHasher {


  public static String sha256Hex(String value) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] digest = md.digest(value.getBytes(StandardCharsets.UTF_8));
      StringBuilder sb = new StringBuilder();
      for (byte b : digest) {
        sb.append(String.format("%02x", b));
      }
      return sb.toString();
    } catch (Exception e) {
      throw new IllegalStateException("SHA-256 not available");
    }
  }
}
