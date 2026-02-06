package br.gov.mt.seletivo_seplag.infra.persistence.repository;

import br.gov.mt.seletivo_seplag.infra.persistence.entity.UsuarioEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<UsuarioEntity, Long> {

  Optional<UsuarioEntity> findByUsuario(String usuario);
}
