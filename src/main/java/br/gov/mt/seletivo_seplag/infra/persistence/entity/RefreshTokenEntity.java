package br.gov.mt.seletivo_seplag.infra.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tb_refresh_token")
public class RefreshTokenEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "usuario_id", nullable = false)
  private UsuarioEntity usuario;

  @Column(name = "token_hash", nullable = false, length = 100)
  private String tokenHash;

  @Column(name = "data_hora_expiracao", nullable = false)
  private OffsetDateTime expiresAt;

  @Default
  @Column(nullable = false)
  private Boolean revogado = false;

  @Default
  @Column(name = "data_cadastro", nullable = false)
  private OffsetDateTime dataCadastro = OffsetDateTime.now();

}
