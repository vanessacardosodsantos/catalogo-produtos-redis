# 🛍️ Product Catalog API

API REST de catálogo de produtos construída com Spring Boot, PostgreSQL e Redis.
Projeto desenvolvido para demonstrar o uso de cache distribuído na prática.

---

## 🚀 Tecnologias

| Tecnologia | Por que foi usada |
|---|---|
| Java 21 | LTS mais recente, suporte a records e melhorias de performance |
| Spring Boot | Framework principal, produtividade e ecossistema maduro |
| Spring Data JPA | Abstração do banco relacional, sem SQL manual |
| Spring Data Redis | Integração com Redis, suporte a @Cacheable e RedisTemplate |
| Spring Cache | Anotações declarativas de cache (@Cacheable, @CacheEvict, @CachePut) |
| PostgreSQL | Banco de dados relacional, fonte de verdade dos dados |
| Redis | Cache distribuído em memória, ranking com Sorted Set |
| Lombok | Elimina boilerplate de getters, setters e construtores |
| Bean Validation | Validação de entrada com @NotBlank, @NotNull, @Positive |
| Docker Compose | Orquestra localmente PostgreSQL, Redis, PgAdmin e Redis Commander |
| PgAdmin 4 | Interface visual para administrar e inspecionar o PostgreSQL |
| Redis Commander | Interface visual para inspecionar chaves e valores no Redis em tempo real |
| Testcontainers | Sobe containers reais de Redis durante os testes de integração — sem mock |
| JUnit 5 + Mockito | Testes unitários do controller com MockMvc e @MockitoBean |
| GitHub Actions | Pipeline de CI que roda os testes automaticamente a cada push ou PR na main |

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

Testa o Redis de verdade — sem mock, sem emulador, sem Redis instalado na máquina.
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