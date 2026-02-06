package br.gov.mt.seletivo_seplag.infra.persistence.repository;

import br.gov.mt.seletivo_seplag.infra.persistence.entity.RefreshTokenEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {

  Optional<RefreshTokenEntity> findFirstByTokenHashAndRevogadoFalse(String tokenHash);
}
