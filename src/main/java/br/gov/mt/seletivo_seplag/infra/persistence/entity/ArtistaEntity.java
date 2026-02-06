package br.gov.mt.seletivo_seplag.infra.persistence.entity;


import br.gov.mt.seletivo_seplag.infra.persistence.entity.enums.TipoArtistaEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
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
@Table(name = "tb_artista")
public class ArtistaEntity extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 200)
  private String nome;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20, name = "tipo_artista")
  private TipoArtistaEnum tipoArtista;

  @Default
  @ManyToMany(mappedBy = "artistas")
  private Set<AlbumEntity> albuns = new HashSet<>();

}
