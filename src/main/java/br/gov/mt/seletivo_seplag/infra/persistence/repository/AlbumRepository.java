package br.gov.mt.seletivo_seplag.infra.persistence.repository;

import br.gov.mt.seletivo_seplag.infra.persistence.entity.AlbumEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AlbumRepository extends JpaRepository<AlbumEntity, Long> {

  @Query("""
        select distinct a from AlbumEntity a
        left join a.artistas ar
        where (:artistName is null or lower(ar.nome) like lower(concat('%', :artistName, '%')))
      """)
  Page<AlbumEntity> findAllFiltered(@Param("artistName") String artistName, Pageable pageable);
}
