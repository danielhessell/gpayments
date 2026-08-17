# Plano: Port go-gateway (Go) → Kotlin/Spring Boot/Maven

> Plano de implementação aprovado que originou este projeto. Mantido aqui como registro histórico das decisões — o estado atual e vivo do sistema está documentado em [`ARCHITECTURE.md`](./ARCHITECTURE.md).

## Context

`imersao22/go-gateway` é microserviço de payment gateway (curso Full Cycle "Imersão22"): API REST (chi router) + Postgres (raw SQL via `lib/pq`) + Kafka (segmentio/kafka-go) para fluxo antifraude assíncrono. Objetivo: portar para novo projeto Kotlin na raiz do repo, usando Java 21 + Spring Boot + Maven, mantendo o contrato de negócio mas corrigindo inconsistências identificadas no Go original.

Novo projeto: `/home/daniel/dev/gpayments/gpayments-gateway/` (sibling de `imersao22/go-gateway`, self-contained, com seu próprio `docker-compose.yml`).

## Decisões confirmadas com usuário

- **Persistência**: Spring Data **JPA/Hibernate** (não JdbcTemplate). Entidades `@Entity` com id atribuído pela aplicação (UUID string, sem `@GeneratedValue` — mesmo padrão do Go). Lock pessimista via `@Lock(LockModeType.PESSIMISTIC_WRITE)` pro update de balance (motivo detalhado em `ARCHITECTURE.md` seção 8).
- **Migrations**: Flyway continua dono do schema (`spring.jpa.hibernate.ddl-auto=validate`, Hibernate não gera DDL).
- **Kafka**: porte completo (producer + consumer) via Spring Kafka, reaproveitando tópicos `pending_transactions` / `transaction_results`.
- **Nome do projeto**: `gpayments-gateway` (groupId `com.gpayments`, artifactId `gpayments-gateway`).
- **Documentação**: `ARCHITECTURE.md` documentando decisões de projeto, contrato de API completo e como o sistema funciona.
- **Bugs/quirks do Go NÃO replicados 1:1** — corrigidos ao portar:
  - Erros HTTP: envelope JSON consistente `{"error": "..."}` via `@RestControllerAdvice` (Go usava `http.Error` texto plano).
  - Header `X-API-KEY` ausente → sempre `401` em todas as rotas (Go variava 400/401).
  - `GET /invoice` sem resultados → `[]` (Go retornava `null`).
  - RNG do `Process()` (seed por segundo) não replicado — usa `ThreadLocalRandom`, mantendo split 70%/30%.
  - Valores monetários: `BigDecimal` (mapeia limpo pra `DECIMAL(10,2)`), não `Double`.

## Regras de negócio a preservar

1. `POST /accounts`: cria conta com UUID, API key aleatória (32 hex chars via `SecureRandom`, 16 bytes), balance=0.
2. `Invoice.amount` deve ser `> 0`, senão erro (400).
3. `cardLastDigits` = últimos 4 chars do número do cartão.
4. Regra de aprovação: `amount > 10000` → fica `pending` (dispara evento Kafka pra antifraude assíncrono); `amount <= 10000` → decisão síncrona 70% aprovado / 30% rejeitado.
5. Em `approved` (síncrono ou via consumer Kafka), soma `amount` ao balance da conta via transação com lock pessimista.
6. Invoice `pending` publica `PendingTransaction{account_id, invoice_id, amount}` no tópico `pending_transactions`.
7. Consumer no tópico `transaction_results` (group `gateway-group`) recebe `{invoice_id, status}`, só permite transição de status a partir de `pending`, aplica balance se `approved`.
8. Auth: header `X-API-KEY` resolve conta; rotas `/invoice/**` exigem; `GET /invoice/{id}` só retorna se `invoice.accountId == caller.accountId` (senão 403).

## Estrutura planejada

```
gpayments-gateway/
├── pom.xml
├── docker-compose.yml
├── ARCHITECTURE.md
├── src/main/kotlin/com/gpayments/gateway/
│   ├── GatewayApplication.kt
│   ├── domain/            (Account, Invoice — @Entity; CreditCard; Status; exception/)
│   ├── dto/                (AccountDtos, InvoiceDtos + mappers toOutput())
│   ├── repository/         (AccountRepository, InvoiceRepository — JpaRepository)
│   ├── service/             (AccountService, InvoiceService)
│   ├── web/
│   │   ├── controller/       (AccountController, InvoiceController)
│   │   ├── filter/           (ApiKeyAuthInterceptor — HandlerInterceptor)
│   │   └── exception/        (GlobalExceptionHandler, ErrorResponse)
│   ├── kafka/                (events/, PendingTransactionProducer, TransactionResultConsumer)
│   └── config/               (WebMvcConfig, KafkaConfig)
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/V1__create_accounts_table.sql
└── test.http
```

## Verificação (executada durante a implementação)

1. `mvn clean package` — build limpo, Flyway valida schema no boot, Hibernate valida entidades contra o schema.
2. `docker compose up -d` — sobe Postgres + Kafka + cria tópicos.
3. `mvn spring-boot:run` / `java -jar target/*.jar` — confirma migration aplicada, consumer Kafka conectado.
4. Testes manuais end-to-end (executados com curl, resultados confirmados):
   - `POST /accounts` → 201, `api_key` capturado.
   - `GET /accounts` com/sem header → 200 / 401 JSON.
   - `POST /invoice` amount=100.50 → 201, status approved/rejected (síncrono).
   - `POST /invoice` amount=15000 → 201 status `pending`; mensagem confirmada no tópico `pending_transactions`.
   - Mensagem fake publicada em `transaction_results` → `GET /invoice/{id}` refletiu novo status + balance da conta atualizado.
   - `GET /invoice/{id}` com API key de outra conta → 403.
   - `GET /invoice/{id}` inexistente → 404.
   - `GET /invoice` sem invoices → `[]`.
   - Rota `/invoice/**` sem `X-API-KEY` → 401 JSON uniforme.

### Bug encontrado e corrigido durante a verificação

A migration original usava `UUID` (tipo nativo do Postgres) pra `id`/`account_id`, mas as entidades JPA mapeiam esses campos como `String` sem type adapter — Hibernate falhava na validação de schema (`wrong column type`) e, após corrigir isso ingenuamente com `@JdbcTypeCode`, falhava em runtime nas comparações SQL (`uuid = character varying`). Solução: trocar a coluna pra `VARCHAR(36)` na migration — a aplicação já gera o UUID como string antes de persistir (igual ao Go), então o tipo nativo `uuid` do Postgres não trazia benefício e só exigia binding extra no Hibernate.
