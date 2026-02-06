package br.gov.mt.seletivo_seplag.infra.persistence.repository;

import br.gov.mt.seletivo_seplag.infra.persistence.entity.RegionalEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionalRepository extends JpaRepository<RegionalEntity, Long> {

  List<RegionalEntity> findByAtivoTrue();

  Optional<RegionalEntity> findFirstByExternalIdAndAtivoTrue(Integer externalId);
}
