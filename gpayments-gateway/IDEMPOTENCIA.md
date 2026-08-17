# Idempotência

## O que é

Operação idempotente: executar 1x ou N vezes dá mesmo resultado final. `PUT /users/1 {name: "Ana"}` chamado 5x deixa estado igual a chamado 1x — idempotente. `POST /accounts` sem proteção, chamado 2x, cria 2 contas — não idempotente.

Importa aqui porque rede falha: client manda request, timeout antes da resposta chegar, client não sabe se serviço processou ou não, client faz retry. Se endpoint não é idempotente, retry duplica efeito (2 contas, 2 invoices, balance incrementado 2x).

Não é sobre response ser igual — é sobre efeito colateral (estado no banco, side effects) não duplicar. Formas de garantir isso neste projeto, usadas abaixo conforme o caso: constraint de unicidade de negócio (`email`), guard de transição de estado (`PENDING → approved`), ou idempotency key genérica (client manda um ID, servidor deduplica).

## Pontos que já são idempotentes por construção

### Consumer Kafka (`transaction_results`)

`Invoice.updateStatus()` em `domain/Invoice.kt:58` só permite transição a partir de `PENDING`:
```kotlin
if (status != Status.PENDING) throw InvalidStatusException()
```
Kafka é *at-least-once* — mesma mensagem pode chegar 2x (rebalance, retry, etc). Reprocessar `{invoice_id, status:"approved"}` pra invoice já `approved`: guard rejeita, `TransactionResultConsumer.kt:26-27` captura a exceção, loga, descarta. Balance não é incrementado de novo. Reprocessamento seguro — nenhuma implementação extra foi necessária, é consequência do modelo de estado (`pending → approved|rejected`, transição única).

## Implementado: `POST /accounts` rejeita email duplicado com 409

Bug separado da idempotência de retry (ver seção abaixo): `email` é `UNIQUE` no schema, mas antes não havia checagem na aplicação — uma segunda conta com o mesmo email quebrava com `500 internal server error` vindo direto da constraint do Postgres, em vez de uma resposta prevísivel.

Fix: `AccountRepository.findByEmail(email)` + checagem em `AccountService.createAccount` antes do insert, lançando `DuplicatedEmailException` → `409` (mesmo padrão já usado pra colisão de `api_key`). Sem tabela nova, sem serviço genérico — só um finder + um `if`.

```kotlin
fun createAccount(input: CreateAccountInput): AccountOutput {
    if (accountRepository.findByEmail(input.email) != null) {
        throw DuplicatedEmailException()
    }
    ...
}
```

Mesma ressalva de sempre pra esse padrão check-then-insert: existe uma janela de corrida (duas requests com o mesmo email exatamente ao mesmo tempo) onde ambas passam pela checagem antes de qualquer uma inserir — nesse caso a constraint `UNIQUE` do banco ainda garante que só uma linha é criada, mas a segunda request recebe `500` (violação de constraint não tratada) em vez de `409`. Raro o suficiente pra não justificar lock/retry aqui; se quiser fechar 100%, o fix é capturar `DataIntegrityViolationException` no `save()` e mapear pra `DuplicatedEmailException` também.

## Considerado e descartado: idempotency key genérica

Cheguei a implementar (e depois reverter) um mecanismo genérico de `Idempotency-Key` — header opcional, tabela `idempotency_keys`, `IdempotencyService` reutilizável — pra resolver retry de rede em `POST /accounts`. Ficou descartado porque **resolvia um problema diferente do que motivou a implementação**: o bug relatado era "retry com mesmo email quebra com 500", que é resolvido pelo fix simples acima (checagem de unicidade de negócio, sem infraestrutura nova).

Idempotency key genérica ainda tem valor pra um problema distinto — replay exato da resposta (incluindo `api_key` gerado) quando o client reenvia a *mesma* requisição após um timeout, sem depender de existir um campo de negócio único pra checar. É a única opção realista pra `POST /invoice`, que não tem nenhum campo natural equivalente a `email`. Não implementado por ora — ver TODO abaixo.

## TODO — não implementado ainda

- **`POST /invoice`**: sem unique key de negócio (diferente de `/accounts`), retry duplica invoice e, se aprovada, duplica o incremento de balance. Não dá pra resolver com um simples `findByX` — precisaria de idempotency key genérica (header `Idempotency-Key` + tabela de dedup, como descrito acima) ou de o client gerar um `id` de invoice e mandar no request (idempotência por client-supplied ID).
- **Kafka producer**: `KafkaConfig.kt` não configura `enable.idempotence`/`acks` explicitamente — no retry de rede do próprio producer, uma mensagem pode ser duplicada em `pending_transactions`. Hoje isso não quebra nada porque o consumer do lado antifraude (fora deste projeto) e o guard de `updateStatus` absorvem duplicidade no "retorno", mas a duplicidade na origem não é tratada.
- **`DataIntegrityViolationException` em `POST /accounts`**: fechar a janela de corrida do check-then-insert descrita acima, se justificar o esforço.
