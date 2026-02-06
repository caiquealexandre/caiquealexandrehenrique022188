package br.gov.mt.seletivo_seplag.infra.persistence.entity;

import br.gov.mt.seletivo_seplag.infra.persistence.entity.enums.PerfilAcesso;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tb_usuario")
public class UsuarioEntity extends BaseEntity {


  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 120)
  private String usuario;

  @Column(name = "password_hash", nullable = false, length = 200)
  private String password;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "tb_usuario_perfis", joinColumns = @JoinColumn(name = "usuario_id"))
  @Enumerated(EnumType.STRING)
  @Column(name = "perfil", nullable = false, length = 30)
  private Set<PerfilAcesso> perfis;

}
