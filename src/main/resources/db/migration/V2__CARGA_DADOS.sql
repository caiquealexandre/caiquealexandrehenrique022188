-- ARTISTAS
insert into tb_artista (nome, tipo_artista)
values ('Serj Tankian', 'SINGER'),
       ('Mike Shinoda', 'SINGER'),
       ('Michel Teló', 'SINGER'),
       ('Guns N'' Roses', 'BAND');

-- ÁLBUNS
insert into tb_album (titulo, ano_lancamento)
values ('Harakiri', 2012),
       ('Black Blooms', null),
       ('The Rough Dog', null),
       ('The Rising Tied', 2005),
       ('Post Traumatic', 2018),
       ('Post Traumatic EP', 2018),
       ('Where''d You Go', 2005),
       ('Bem Sertanejo', null),
       ('Bem Sertanejo - O Show (Ao Vivo)', null),
       ('Bem Sertanejo - (1ª Temporada) - EP', null),
       ('Use Your Illusion I', 1991),
       ('Use Your Illusion II', 1991),
       ('Greatest Hits', 2004);

-- VINCULA ARTISTA x ÁLBUM (N:N)
insert into tb_artista_album (album_id, artista_id)
select al.id, ar.id
from tb_album al
         cross join tb_artista ar
where (al.titulo in ('Harakiri', 'Black Blooms', 'The Rough Dog') and ar.nome = 'Serj Tankian')
   or (
    al.titulo in ('The Rising Tied', 'Post Traumatic', 'Post Traumatic EP', 'Where''d You Go') and
    ar.nome = 'Mike Shinoda')
   or (al.titulo like 'Bem Sertanejo%' and ar.nome = 'Michel Teló')
   or (al.titulo in ('Use Your Illusion I', 'Use Your Illusion II', 'Greatest Hits') and
       ar.nome = 'Guns N'' Roses');

-- Usuário admin (senha: admin) -> gere hash BCrypt e cole aqui
-- insert into tb_usuario (usuario, password_hash) values ('admin', '<BCryptHash>');
-- insert into tb_usuario_perfis (usuario_id, perfil) values (<id_admin>, 'ROLE_ADMIN');
