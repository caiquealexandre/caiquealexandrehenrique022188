create table tb_artista
(
    id             bigserial primary key,
    nome           varchar(200) not null,
    tipo_artista   varchar(20)  not null,
    data_cadastro  timestamptz  not null default now(),
    data_alteracao timestamptz,
    status         varchar(10)  not null default 'ATIVO'
);

create table tb_album
(
    id             bigserial primary key,
    titulo         varchar(200) not null,
    ano_lancamento int,
    data_cadastro  timestamptz  not null default now(),
    data_alteracao timestamptz,
    status         varchar(10)  not null default 'ATIVO'
);

create table tb_artista_album
(
    album_id   bigint not null references tb_album (id),
    artista_id bigint not null references tb_artista (id),
    primary key (album_id, artista_id)
);

create table tb_album_image
(
    id             bigserial primary key,
    album_id       bigint       not null references tb_album (id),
    object_key     varchar(500) not null,
    content_type   varchar(120),
    size_bytes     bigint,
    data_cadastro  timestamptz  not null default now(),
    data_alteracao timestamptz,
    status         varchar(10)  not null default 'ATIVO'
);

create table tb_usuario
(
    id            bigserial primary key,
    usuario       varchar(120) not null unique,
    password_hash varchar(200) not null
);

create table tb_usuario_perfis
(
    usuario_id bigint      not null references tb_usuario (id),
    perfil     varchar(30) not null
);

create table tb_refresh_token
(
    id                  bigserial primary key,
    usuario_id          bigint       not null references tb_usuario (id),
    token_hash          varchar(100) not null,
    data_hora_expiracao timestamptz  not null,
    revogado            boolean      not null default false,
    data_cadastro       timestamptz  not null default now()
);

create table tb_regional
(
    id            bigserial primary key,
    external_id   int          not null,
    nome          varchar(200) not null,
    ativo         boolean      not null default true,
    data_cadastro timestamptz  not null default now()
);

create index idx_regional_external_ativo on tb_regional (external_id, ativo);
