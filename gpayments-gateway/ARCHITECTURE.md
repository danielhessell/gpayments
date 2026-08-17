# gpayments-gateway

API de payment gateway em Kotlin/Spring Boot — porte de `imersao22/go-gateway` (Go).

## 1. Visão geral

Serviço REST que gerencia contas (`accounts`) e faturas/pagamentos (`invoices`). Faturas acima de R$ 10.000 entram em fluxo assíncrono de antifraude via Kafka; faturas menores são aprovadas/rejeitadas de forma síncrona (70%/30%).

**Stack**: Kotlin, Spring Boot 3.3, Java 21, Maven, Spring Data JPA/Hibernate, Flyway, Spring Kafka, PostgreSQL.

**Origem**: porte funcional de `imersao22/go-gateway` (Go + chi + `lib/pq` + `segmentio/kafka-go`). O contrato de negócio foi preservado; algumas inconsistências do serviço original foram corrigidas (ver seção 2).

## 2. Decisões de projeto

| Decisão | Motivo |
|---|---|
| Spring Data JPA/Hibernate (não JdbcTemplate) | Idiomático em projetos Spring; lock pessimista via `@Lock(PESSIMISTIC_WRITE)` cobre o mesmo caso de uso do `SELECT ... FOR UPDATE` do Go (detalhes na seção 8). |
| Flyway dono do schema (`ddl-auto=validate`) | Hibernate nunca gera DDL; schema versionado e auditável, igual ao `golang-migrate` do Go. |
| Spring Kafka (producer + consumer) | Porte completo do fluxo antifraude assíncrono, reaproveitando os tópicos `pending_transactions`/`transaction_results`. Serialização manual via Jackson (não `JsonSerializer` do Spring) pra manter o wire format em JSON puro, igual ao Go. |
| Erros em envelope JSON `{"error": "..."}` | Go usava `http.Error` (texto plano) — inconsistente com o resto da API que é JSON. Centralizado em `GlobalExceptionHandler`. |
| `X-API-KEY` ausente → sempre `401` | Go retornava `400` em algumas rotas e `401`/`500` em outras pra mesma condição. Agora uniforme em todas as rotas. |
| `GET /invoice` sem resultados → `[]` | Go retornava `null` (zero-value de slice em Go serializado pelo `encoding/json`). |
| Aprovação síncrona via `ThreadLocalRandom` | Go usava RNG seedado por segundo (todas as faturas no mesmo segundo tinham o mesmo resultado) — comportamento não intencional, não replicado. Mantido o split 70% aprovado / 30% rejeitado. |
| `BigDecimal` para valores monetários | Go usava `float64`. `BigDecimal` evita erro de precisão de ponto flutuante e mapeia diretamente pra `DECIMAL(10,2)`. |
| `HandlerInterceptor` (não `Filter`) pra autenticação | Exceções lançadas dentro de um `Filter` bruto não passam pelo `@RestControllerAdvice`; interceptor sim, permitindo resposta de erro uniforme. |

## 3. Contrato de API

Todas as respostas usam JSON `snake_case` (`spring.jackson.property-naming-strategy=SNAKE_CASE`). Erros sempre no formato `{"error": "mensagem"}`.

### `POST /accounts`
Sem autenticação.

Request:
```json
{ "name": "John Doe", "email": "john@example.com" }
```
Response `201`:
```json
{
  "id": "uuid", "name": "John Doe", "email": "john@example.com",
  "balance": 0, "api_key": "32-hex-chars",
  "created_at": "2026-08-17T12:00:00Z", "updated_at": "2026-08-17T12:00:00Z"
}
```

### `GET /accounts`
Header `X-API-Key` obrigatório. Response `200`: mesmo shape de `AccountOutput` acima.

### `POST /invoice`
Header `X-API-KEY` obrigatório.

Request:
```json
{
  "amount": 100.50, "description": "...", "payment_type": "credit_card",
  "card_number": "4111111111111111", "cvv": "123",
  "expiry_month": 12, "expiry_year": 2025, "cardholder_name": "John Doe"
}
```
Response `201`:
```json
{
  "id": "uuid", "account_id": "uuid", "amount": 100.50,
  "status": "approved", "description": "...", "payment_type": "credit_card",
  "card_last_digits": "1111", "created_at": "...", "updated_at": "..."
}
```

### `GET /invoice/{id}`
Header `X-API-KEY` obrigatório. Só retorna se a invoice pertencer à conta do caller. Response `200`: `InvoiceOutput`.

### `GET /invoice`
Header `X-API-KEY` obrigatório. Response `200`: array de `InvoiceOutput` (`[]` se vazio).

### Mapeamento de erros

| Exceção | HTTP |
|---|---|
| `AccountNotFoundException`, `ApiKeyRequiredException` | 401 |
| `DuplicatedApiKeyException` | 409 |
| `InvoiceNotFoundException` | 404 |
| `UnauthorizedAccessException` | 403 |
| `InvalidAmountException`, `InvalidStatusException`, validação (`@Valid`) | 400 |
| genérico | 500 |

## 4. Regras de negócio

1. `POST /accounts` gera UUID + API key aleatória (32 hex chars, `SecureRandom` 16 bytes) + balance inicial 0.
2. `Invoice.amount` deve ser `> 0`, senão `400`.
3. `cardLastDigits` = últimos 4 caracteres do número do cartão.
4. `amount > 10000` → fatura fica `pending`, evento publicado em `pending_transactions` (fluxo antifraude assíncrono); `amount <= 10000` → decisão síncrona 70% `approved` / 30% `rejected`.
5. Em `approved` (síncrono ou via Kafka), balance da conta é incrementado com lock pessimista (`@Lock(PESSIMISTIC_WRITE)` em `AccountRepository.findByIdForUpdate`).
6. Consumer do tópico `transaction_results` (group `gateway-group`) só permite transição de status a partir de `pending` (senão erro, logado e descartado — não derruba o listener).
7. `GET /invoice/{id}` faz ownership check: `invoice.accountId == caller.accountId`, senão `403`.

## 5. Eventos Kafka

**Produzido** — tópico `pending_transactions` (quando invoice fica `pending`):
```json
{ "account_id": "uuid", "invoice_id": "uuid", "amount": 15000.00 }
```

**Consumido** — tópico `transaction_results`, group `gateway-group`:
```json
{ "invoice_id": "uuid", "status": "approved" }
```

## 6. Modelo de dados

Schema criado via Flyway (`src/main/resources/db/migration/V1__create_accounts_table.sql`):

- `accounts(id VARCHAR(36) PK, name, email UNIQUE, api_key UNIQUE, balance DECIMAL(10,2), created_at, updated_at)` — índices em `api_key`, `email`.
- `invoices(id VARCHAR(36) PK, account_id FK→accounts, amount DECIMAL(10,2), status, description, payment_type, card_last_digits, created_at, updated_at)` — índices em `account_id`, `status`, `created_at`.

`id`/`account_id` armazenam UUID como string (`VARCHAR(36)`, não o tipo nativo `uuid` do Postgres) — o id é gerado pela aplicação (igual ao Go) e mapeado como `String` na entidade JPA; usar o tipo `uuid` nativo exigiria binding explícito (`PGobject`/`@JdbcTypeCode`) no Hibernate, desnecessário aqui.

## 7. Como rodar localmente

```bash
# infra (Postgres + Kafka + criação de tópicos)
docker compose up -d

# build
mvn clean package

# run
mvn spring-boot:run
```

Variáveis de ambiente (todas com default, ver `application.yml`):
`HTTP_PORT`, `DB_HOST`, `DB_PORT`, `DB_USER`, `DB_PASSWORD`, `DB_NAME`, `DB_SSL_MODE`, `KAFKA_BROKER`, `KAFKA_PRODUCER_TOPIC`, `KAFKA_CONSUMER_TOPIC`, `KAFKA_CONSUMER_GROUP_ID`.

Testes manuais: usar `test.http` (VS Code REST Client) ou curl, seguindo o contrato da seção 3.

## 8. Concorrência: por que lock pessimista no balance

`Account.balance` é um recurso mutável compartilhado, alterado a partir de **dois caminhos concorrentes**:

- **Síncrono**: `POST /invoice` com `amount <= 10000` decide aprovar/rejeitar na hora e já chama `AccountService.updateBalance`.
- **Assíncrono**: `TransactionResultConsumer` (Kafka, tópico `transaction_results`) chama o mesmo `updateBalance` quando o antifraude aprova uma invoice que ficou `pending`.

Nada impede que duas requisições — ou uma requisição e uma mensagem Kafka — cheguem pra **mesma conta** ao mesmo tempo. Sem controle de concorrência, ocorre um *lost update* clássico:

| Passo | TX1 (invoice A, +50) | TX2 (invoice B, +30) |
|---|---|---|
| 1 | lê balance = 100 | |
| 2 | | lê balance = 100 (antes de TX1 commitar) |
| 3 | grava balance = 150, commita | |
| 4 | | grava balance = 130, commita |

Resultado final: **130**. Esperado: **180** (100 + 50 + 30). O incremento de TX1 foi silenciosamente perdido porque TX2 leu o valor antigo e sobrescreveu com base nele.

**Como o lock resolve**: `AccountRepository.findByIdForUpdate` usa `@Lock(LockModeType.PESSIMISTIC_WRITE)`, que o Hibernate traduz pra `SELECT ... FOR UPDATE` no Postgres. Isso trava a linha da conta no banco assim que lida dentro de uma transação — qualquer outra transação que tente ler a mesma linha com lock **bloqueia até a primeira commitar**. Reaplicando o cenário acima:

1. TX1 roda `SELECT balance FOR UPDATE` → lê 100, trava a linha.
2. TX2 tenta o mesmo `SELECT ... FOR UPDATE` → **bloqueia**, espera TX1.
3. TX1 soma 50, grava 150, commita → libera o lock.
4. TX2 agora lê 150 (valor já atualizado), soma 30, grava 180, commita.

Resultado final: **180**, correto.

```kotlin
interface AccountRepository : JpaRepository<Account, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.id = :id")
    fun findByIdForUpdate(@Param("id") id: String): Account?
}

@Transactional
fun updateBalance(apiKey: String, amount: BigDecimal): AccountOutput {
    val account = findAccountByApiKey(apiKey)
    val locked = accountRepository.findByIdForUpdate(account.id) ?: throw AccountNotFoundException()
    locked.addBalance(amount)          // dirty checking do Hibernate persiste no commit
    return locked.toOutput()
}
```

O método precisa estar dentro de `@Transactional` — é a transação do banco que sustenta o lock; ela só é liberada no commit/rollback. Essa é a mesma técnica usada no Go original (`BEGIN; SELECT ... FOR UPDATE; UPDATE; COMMIT` manual em `internal/repository/account_repository.go`), só que expressa de forma declarativa via Spring Data JPA em vez de SQL escrito à mão — daí a decisão de manter JPA/Hibernate em vez de trocar por JdbcTemplate: o mecanismo de lock pessimista é suportado nativamente pela anotação, sem perder a semântica de `FOR UPDATE`.

**Trade-off consciente**: lock pessimista serializa updates concorrentes na mesma conta (a segunda transação espera, não falha) — correto para este caso porque contenção é rara (updates de balance por conta são esporádicos) e a alternativa (lock otimista com retry) adicionaria complexidade sem necessidade real aqui.

## 9. Estrutura de pacotes

```
com.gpayments.gateway
├── domain/            entidades JPA (Account, Invoice), enum Status, exceções
├── dto/                request/response DTOs + mappers toOutput()
├── repository/         interfaces Spring Data JPA
├── service/             orquestração de negócio (AccountService, InvoiceService)
├── web/controller/       REST controllers
├── web/filter/           ApiKeyAuthInterceptor (HandlerInterceptor)
├── web/exception/        GlobalExceptionHandler + ErrorResponse
├── kafka/                producer, consumer, eventos
└── config/               WebMvcConfig, KafkaConfig
```
