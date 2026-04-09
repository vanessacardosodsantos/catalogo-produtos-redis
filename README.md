# 🛍️ Product Catalog API

API REST de catálogo de produtos construída com Spring Boot, PostgreSQL e Redis.
Projeto desenvolvido para demonstrar o uso de cache distribuído na prática.

---

## 🚀 Tecnologias

| Tecnologia | Papel no projeto |
|---|---|
| ![Java](https://img.shields.io/badge/Java-21-blue) | LTS atual, uso de recursos modernos (records, performance otimizada) |
| ![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-brightgreen) | Framework base para construção da API REST |
| ![JPA](https://img.shields.io/badge/Spring_Data_JPA-ORM-blue) | Persistência com abstração ORM sobre banco relacional |
| ![Redis](https://img.shields.io/badge/Spring_Data_Redis-integration-red) | Integração com Redis para cache e operações avançadas |
| ![Cache](https://img.shields.io/badge/Spring_Cache-abstraction-orange) | Estratégia de cache declarativa (@Cacheable, @CacheEvict) |
| ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-database-blue) | Banco relacional como fonte de verdade |
| ![Redis](https://img.shields.io/badge/Redis-cache-red) | Cache em memória para otimização de leitura e ranking |
| ![Lombok](https://img.shields.io/badge/Lombok-boilerplate--reduction-pink) | Redução de código repetitivo |
| ![Validation](https://img.shields.io/badge/Bean_Validation-JSR380-yellow) | Validação de dados de entrada |
| ![Docker](https://img.shields.io/badge/Docker_Compose-orchestration-blue) | Orquestração local dos serviços |
| ![PgAdmin](https://img.shields.io/badge/PgAdmin-UI-blue) | Administração visual do PostgreSQL |
| ![Redis Commander](https://img.shields.io/badge/Redis_Commander-UI-red) | Inspeção de dados em cache |
| ![Testcontainers](https://img.shields.io/badge/Testcontainers-integration--tests-purple) | Testes com containers reais |
| ![Tests](https://img.shields.io/badge/JUnit5%20%2B%20Mockito-unit--tests-success) | Testes unitários e isolamento de camadas |
| ![CI](https://img.shields.io/badge/GitHub_Actions-CI/CD-black) | Pipeline automatizado de build e testes |

---


## ⚙️ Como rodar

### Pré-requisitos

- Java 21
- Docker e Docker Compose instalados

### 2. Suba os serviços de infraestrutura
```bash
docker compose up -d
```

### 3. Verifique se os containers estão rodando
```bash
docker ps
```
Você deve ver os containers `postgres`, `redis`, `pgadmin` e `redis-commander` com status `Up`.

### 4. Rode a aplicação

Abra o projeto na IDE e execute a classe principal, ou via terminal:
```bash
./mvnw spring-boot:run
```

A API estará disponível em `http://localhost:8080`.

---
## 🔗 Serviços disponíveis

| Serviço | URL | Credenciais |
|---|---|---|
| API REST | http://localhost:8080 | — |
| PgAdmin | http://localhost:5050 | admin@admin.com / admin123 |
| Redis Commander | http://localhost:8081 | — |

### Conectar PgAdmin ao banco
Após abrir o PgAdmin, clique em **Add New Server**:

- **Host:** `postgres` (nome do container, não localhost)
- **Port:** `5432`
- **Database:** `catalogdb`
- **Username:** `admin`
- **Password:** `admin123`
---

## 📦 Endpoints
| Método | Endpoint | Descrição |
|---|---|---|
| GET | /products | Lista todos os produtos |
| GET | /products/{id} | Busca produto por ID |
| POST | /products | Cria novo produto |
| PUT | /products/{id} | Atualiza produto |
| DELETE | /products/{id} | Remove produto |

### Exemplo de payload
```json
{
  "name": "Notebook Pro",
  "description": "Notebook com 16GB RAM e SSD 512GB",
  "price": 4999.90,
  "quantity": 50
}
```
---

## 🔄 Fluxo principal de cache
```

GET /products/1 — primeira chamada
│
├── Service verifica o Redis
│     └── não encontrou (Cache Miss)
│
├── vai ao PostgreSQL
│     └── encontrou o produto
│
├── salva uma cópia no Redis com TTL de 10 minutos
│
└── retorna o produto → ~100ms

GET /products/1 — segunda chamada
│
├── Service verifica o Redis
│     └── encontrou! (Cache Hit)
│
└── retorna direto do Redis → ~1ms (não chega no PostgreSQL)

PUT /products/1 — atualização
│
├── salva dado novo no PostgreSQL (fonte de verdade)
│
├── invalida o cache do produto id=1 no Redis (@CacheEvict)
│
└── próxima leitura vai buscar o dado atualizado no PostgreSQL
```
### Testes de integração com Testcontainers

O Testcontainers sobe um container real de `redis:7` antes dos testes
e derruba automaticamente ao final.

- `@Testcontainers` gerencia o ciclo de vida do container no JUnit
- `GenericContainer("redis:7")` sobe a imagem oficial do Redis
- `@DynamicPropertySource` injeta o host e a porta do container no Spring antes de subir o contexto
- Cobre os cenários: salvar e recuperar valor, incremento (INCR), decremento (DECR), expiração por TTL e ranking com Sorted Set

**Por que Testcontainers em vez de mock?**
Mockar o Redis esconde comportamentos reais — serialização, expiração de TTL,
operações atômicas e estruturas como Sorted Set só se provam com um Redis real.
Testcontainers garante que o teste valida o comportamento real sem depender
de infraestrutura externa instalada na máquina.

### Pipeline de CI com GitHub Actions

A cada push ou pull request na branch `main`, o GitHub Actions executa
automaticamente todos os testes do projeto em uma máquina virtual Ubuntu.

- Trigger em `push` e `pull_request` na `main`
- Provisiona Java 21 com `actions/setup-java@v4`
- Executa `./gradlew test`
- Publica o relatório HTML de testes como artefato para download
- Check verde no commit indica que todos os testes passaram
- Check vermelho bloqueia o merge e aponta qual teste falhou

## 👨‍💻 Autor

Feito por **Van** · [LinkedIn](https://linkedin.com/in/seu-perfil) 
