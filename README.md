# 🛍️ Product Catalog API

API REST de catálogo de produtos construída com Spring Boot, PostgreSQL e Redis.
Projeto desenvolvido para demonstrar o uso de cache distribuído na prática.

---

## 🚀 Tecnologias

| Tecnologia | Versão | Por que foi usada |
|---|---|---|
| Java | 21 | LTS mais recente, suporte a records e melhorias de performance |
| Spring Boot | 3.5.13 | Framework principal, produtividade e ecossistema maduro |
| Spring Data JPA | 3.5.13 | Abstração do banco relacional, sem SQL manual |
| Spring Data Redis | 3.5.13 | Integração com Redis, suporte a @Cacheable e RedisTemplate |
| Spring Cache | 3.5.13 | Anotações declarativas de cache (@Cacheable, @CacheEvict) |
| PostgreSQL | 16 | Banco de dados relacional, fonte de verdade dos dados |
| Redis | 7 | Cache distribuído em memória, ranking com Sorted Set |
| Lombok | latest | Elimina boilerplate de getters, setters e construtores |
| Bean Validation | 3.x | Validação de entrada com @NotBlank, @NotNull, @Positive |
| Docker Compose | latest | Orquestração local dos serviços de infraestrutura |
| PgAdmin 4 | latest | Interface visual para administrar o PostgreSQL |
| Redis Commander | latest | Interface visual para inspecionar dados no Redis |

---


### Quando o Dockerfile vai entrar?

O Dockerfile será adicionado no **Nível 4** do roadmap, quando a aplicação
for empacotada em container para rodar via GitHub Actions e ser publicada
como imagem Docker. Nesse momento, o Docker Compose também passará a
incluir a aplicação, permitindo subir tudo com um único `docker compose up`.

---

## ⚙️ Como rodar

### Pré-requisitos

- Java 21
- Maven
- Docker e Docker Compose instalados

### 1. Clone o repositório
```bash
git clone https://github.com/seu-usuario/product-catalog-api.git
cd product-catalog-api
```

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

### Por que esse fluxo?

O padrão utilizado é o **Cache-Aside** (também chamado de Lazy Loading).
A aplicação é responsável por consultar o cache antes do banco e por
popular o cache após um miss. Isso foi escolhido porque:

- Produtos são lidos com muito mais frequência do que atualizados
- Toleramos que o dado fique desatualizado por até 10 minutos (TTL)
- Em caso de falha do Redis, a aplicação continua funcionando normalmente via banco

---



## 🗺️ Roadmap

- [x] Nível 1 — CRUD + Cache básico + Docker Compose
- [ ] Nível 2 — Rate limiting + Ranking + Testes com Testcontainers
- [ ] Nível 3 — Design Patterns + Circuit Breaker + Resilience4j
- [ ] Nível 4 — CI/CD + Dockerfile + Métricas com Grafana
- [ ] Nível 5 — Kubernetes + Redis Cluster + Cache L1/L2

---

## 👨‍💻 Autor

Feito por **Van** · [LinkedIn](https://linkedin.com/in/seu-perfil) 