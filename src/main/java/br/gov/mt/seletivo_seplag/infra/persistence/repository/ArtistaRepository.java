package br.gov.mt.seletivo_seplag.infra.persistence.repository;

import br.gov.mt.seletivo_seplag.infra.persistence.entity.ArtistaEntity;
import br.gov.mt.seletivo_seplag.infra.persistence.entity.enums.TipoArtistaEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArtistaRepository extends JpaRepository<ArtistaEntity, Long> {

  Page<ArtistaEntity> findByNameContainingIgnoreCase(String name,
      Pageable pageable);

  Page<ArtistaEntity> findByNameContainingIgnoreCaseAndType(String name,
      TipoArtistaEnum tipoArtistaEnum,
      Pageable pageable);

}
