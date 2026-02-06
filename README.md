# Music API (Projeto Prático - Back-end Java Sênior)

API REST para cadastro e consulta de **Artistas** e **Álbuns**, com relacionamento **N:N**, autenticação **JWT** com expiração curta e renovação (refresh), **paginação**, **filtros/ordenação**, **upload de imagens** para **MinIO (S3)** com geração de **presigned URLs**, **WebSocket** para notificação em tempo real de novos álbuns, **rate limit** por usuário, **CORS** restritivo e **sincronização** com endpoint externo de “regionais”.

---

## Dados de Inscrição

- **Nome:** Caíque Alexandre Henrique
- **CPF:** 022.188.* * * - * *
- **E-mail:** caique.alexandreh@gmail.com
- **Vaga:** Desenvolvedor Back-end Sênior

---

## Stack e Tecnologias

- Java 17+
- Spring Boot 3.x
- Spring Web + Validation
- Spring Data JPA (Hibernate)
- Spring Security (JWT)
- Flyway (migrations)
- PostgreSQL
- MinIO (S3)
- WebSocket (STOMP)
- Spring Actuator (Health/Liveness/Readiness)
- OpenAPI/Swagger (springdoc)
- Rate Limit: Bucket4j
- Testes: JUnit 5 + Spring Boot Test + Testcontainers (PostgreSQL)

---

## Arquitetura e Estrutura do Projeto

### Visão Geral

A aplicação segue uma arquitetura em camadas com separação clara de responsabilidades:

- **api/**: camada de entrada (controllers, DTOs, mappers, websocket)
- **domain/**: regras de negócio (services), modelos e exceções
- **infra/**: persistência (JPA entities/repositories), integrações externas, MinIO
- **security/**: autenticação/autorização JWT, filtros e rate limit
- **config/**: configurações transversais (CORS, OpenAPI, WebSocket, etc.)

### Estrutura de Pacotes (exemplo)

src/main/java/com/seugrupo/music
config/
security/
api/
v1/
controller/
dto/
mapper/
websocket/
domain/
model/
service/
exception/
infra/
persistence/
entity/
repository/
minio/
regionais/


### Decisões Técnicas

- **Versionamento de API:** `/api/v1/...` para suportar evolução sem quebrar clientes.
- **Banco + Migrations:** Flyway com `ddl-auto=validate` para garantir consistência do schema.
- **JWT:** Access token com expiração curta (5 min) + endpoint de refresh para renovar.
- **Rate Limit:** 10 requisições por minuto **por usuário** (claim `sub` do JWT).
- **CORS restritivo:** permite apenas origens configuradas (bloqueia domínios externos).
- **MinIO:** imagens armazenadas em bucket; retorno via **presigned URLs** (30 min).
- **WebSocket:** notifica o front ao cadastrar novo álbum (STOMP topic).
- **Sincronização de regionais:** integra com endpoint externo e aplica regras de inativação/versão.

---

## Modelagem de Dados

### Relacionamento N:N (Artista–Álbum)

- Um artista pode estar em vários álbuns.
- Um álbum pode ter vários artistas.

Tabela associativa: `artist_album(artist_id, album_id)`

### Tabelas principais (resumo)

- `tb_artista`: artistas (cantores/bandas)
- `tb_album`: álbuns
- `tb_artista_album`: relacionamento N:N
- `tb_album_image`: metadados das imagens armazenadas no MinIO
- `tb_usuario`: usuários do sistema (para autenticação)
- `tb_regional`: tabela interna sincronizada do endpoint externo

### Seed (dados iniciais)

O projeto inclui carga inicial com exemplos no Flyway (ex.: Serj Tankian, Mike Shinoda, Michel Teló, Guns N’ Roses e álbuns listados no enunciado).

---

## Segurança

### Autenticação JWT

- **Access token:** expira em **5 minutos**
- **Refresh token:** permite renovar o access token (sem precisar logar novamente)

Endpoints:
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`

### CORS

Acesso permitido somente para domínios definidos em configuração (`app.api.allowed-origins`).

### Rate Limit (10/min por usuário)

- Implementado via filtro (Bucket4j) com chave por usuário autenticado.
- Ao estourar limite: retorna **HTTP 429**.

---

## Upload de Imagens + MinIO (S3)

- Upload de **uma ou mais** imagens para um álbum:
    - `POST /api/v1/albums/{albumId}/images` (multipart/form-data)
- Consulta imagens do álbum com **links temporários**:
    - `GET /api/v1/albums/{albumId}/images`
- **Presigned URLs** expiram em **30 minutos**.

---

## WebSocket (Notificação de novo álbum)

- Endpoint WebSocket: `/ws`
- Topic de notificação:
    - `/topic/albums`

Ao cadastrar um novo álbum, o backend publica um evento para que o front seja notificado em tempo real.

---

## Sincronização de Regionais (Sênior)

Endpoint externo:
- `GET https://integrador-argus-api.geia.vip/v1/regionais`

Importação para tabela interna:
- `POST /api/v1/regionais/sync`

Regras de sincronização (menor complexidade):
1. **Novo** no endpoint externo → inserir ativo.
2. **Ausente** no endpoint externo → inativar registro ativo.
3. **Alterado** (ex.: nome mudou) → inativar o antigo e inserir novo registro ativo.

---

## Health Checks (Liveness/Readiness)

Spring Actuator expõe:
- `GET /actuator/health`
- `GET /actuator/health/liveness`
- `GET /actuator/health/readiness`

---

## Documentação OpenAPI/Swagger

- Swagger UI:
    - `/swagger-ui/index.html`
- OpenAPI JSON:
    - `/v3/api-docs`

---

## Como Executar

### 1) Subir com Docker Compose (recomendado)

Pré-requisitos:
- Docker e Docker Compose

Comandos:

```bash
docker compose up -d --build
