package br.gov.mt.seletivo_seplag.infra.persistence.repository;

import br.gov.mt.seletivo_seplag.infra.persistence.entity.AlbumImageEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlbumImageRepository extends JpaRepository<AlbumImageEntity, Long> {

  List<AlbumImageEntity> findByAlbumId(Long albumId);
}
