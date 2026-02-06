package br.gov.mt.seletivo_seplag.infra.persistence.entity;

import br.gov.mt.seletivo_seplag.infra.persistence.entity.enums.StatusEnum;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@MappedSuperclass
public abstract class BaseEntity {

  @Column(name = "data_cadastro", nullable = false, updatable = false)
  private LocalDateTime dataCadastro;

  @Column(name = "data_alteracao", nullable = true)
  private LocalDateTime dataAlteracao;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 10)
  private StatusEnum status;

  @PrePersist
  public void prePersist() {
    this.dataCadastro = LocalDateTime.now();
    this.status = StatusEnum.ATIVO;
  }

  @PreUpdate
  public void PreUpdate() {
    this.dataAlteracao = LocalDateTime.now();
  }
}
