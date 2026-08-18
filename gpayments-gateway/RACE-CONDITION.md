# Race Condition no balance da Account

## O que é o problema

Quando duas ou mais transações concorrentes tentam atualizar o `balance` da mesma `Account` ao mesmo tempo, pode ocorrer uma **race condition**: ambas leem o mesmo valor antigo, aplicam sua alteração em memória, e uma das escritas sobrescreve a outra. Resultado: dinheiro "some" do balance, porque um dos incrementos é perdido (lost update).

Exemplo prático:
1. Balance atual: R$ 100
2. Transação A lê balance = 100, soma +50 → vai escrever 150
3. Transação B lê balance = 100 (antes de A terminar), soma +30 → vai escrever 130
4. B escreve por último → balance final = 130 (deveria ser 180)

## Como foi resolvido

Usamos **pessimistic lock** (lock pessimista) a nível de linha no banco, via JPA/Hibernate, em `AccountRepository.findByIdForUpdate()`:

```kotlin
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("select a from Account a where a.id = :id")
fun findByIdForUpdate(@Param("id") id: String): Account?
```

Isso faz o Hibernate gerar um `SELECT ... FOR UPDATE`. Quando `AccountService.updateBalance()` (dentro de `@Transactional`) busca a account por esse método, o banco trava a linha daquela account até a transação terminar (commit ou rollback). Qualquer outra transação que tente ler essa mesma linha com lock fica **bloqueada, esperando** — só segue depois que a primeira liberar.

```kotlin
@Transactional
fun updateBalance(apiKey: String, amount: BigDecimal): AccountOutput {
    val account = findAccountByApiKey(apiKey)
    val locked = accountRepository.findByIdForUpdate(account.id) ?: throw AccountNotFoundException()
    locked.addBalance(amount)
    return locked.toOutput()
}
```

Com isso, transações concorrentes na mesma account viram uma fila: uma de cada vez lê, altera e confirma o balance, eliminando o lost update.

## O que é "lock"

Lock é um mecanismo de exclusão mútua: garante que só um processo/transação por vez pode ler ou escrever um dado protegido. Enquanto o lock está ativo, quem quiser mexer no mesmo dado precisa esperar (ou falhar, dependendo da estratégia).

## Tipos de lock possíveis

- **Pessimistic lock (lock pessimista)** — o que usamos aqui. Trava a linha no banco assim que é lida (`SELECT ... FOR UPDATE`). Assume que conflito é provável, então bloqueia na largada. Simples e seguro, mas pode gerar espera (contenção) se muitas transações disputarem a mesma account.

- **Optimistic lock (lock otimista)** — não trava nada na leitura. Usa uma coluna de versão (`@Version` no JPA); ao salvar, compara a versão lida com a atual no banco. Se mudou, lança erro (`OptimisticLockException`) e quem chamou precisa tentar de novo. Bom quando conflito é raro e queremos evitar bloqueio, mas exige lógica de retry.

- **Lock a nível de aplicação (mutex/semaphore em memória)** — trava via código (ex: `synchronized`, `ReentrantLock`, semáforo) dentro do próprio processo. Só funciona se a aplicação rodar numa única instância; não protege contra concorrência entre múltiplas instâncias/pods acessando o mesmo banco.

- **Lock distribuído (Redis, Zookeeper, etc)** — como o mutex, mas coordenado entre múltiplas instâncias da aplicação. Útil quando o lock não pode ser feito no banco ou quando o recurso protegido não é uma linha de banco.

## Por que pessimistic lock aqui, e não os outros

- **Vs. optimistic lock**: balance de conta é dado sensível e conflitos de escrita concorrente (várias transações na mesma account) não são raros em um gateway de pagamentos. Optimistic lock exigiria retry manual em cada conflito, complicando o fluxo. Pessimistic lock resolve de forma direta: quem chega depois só espera a vez, sem precisar re-tentar a operação.

- **Vs. mutex de aplicação**: a aplicação pode rodar em múltiplas instâncias (múltiplos pods/processos). Um lock em memória (`synchronized`) só protege dentro de uma instância — duas instâncias diferentes ainda colidiriam no banco. O lock pessimista do banco protege independente de quantas instâncias da aplicação existirem, porque a garantia vem do próprio banco de dados.

- **Vs. lock distribuído externo**: adicionaria uma dependência extra (Redis/Zookeeper) e complexidade operacional para resolver algo que o próprio banco relacional já garante nativamente via `FOR UPDATE`, dentro da mesma transação que já protege o resto da operação.

## Problemas possíveis com o pessimistic lock

- **Contenção**: account com volume alto (merchant grande) vira gargalo — todas as transações na mesma account serializam, fila cresce, latência sobe.
- **Deadlock**: se lock de múltiplas accounts for adquirido em ordens diferentes (ex: transferência A→B numa thread, B→A noutra), o banco derruba uma das transações.
- **Lock segurado tempo demais**: se dentro do `@Transactional` rolar uma chamada externa lenta (webhook, gateway) enquanto o lock está ativo, ele fica preso mais tempo, bloqueando mais gente.
- **Esgotamento de connection pool**: transações esperando o lock seguram conexão do pool. Sob concorrência alta, o pool esgota e outras operações, nem relacionadas ao lock, começam a falhar.
- **Não escala horizontalmente**: o lock é por instância de banco. Se a account for sharded/replicada, o lock não cobre todas as réplicas.
- **Timeout em cascata**: espera longa → timeout → retry do client → mais carga → mais espera.

## Alternativas ao pessimistic lock

1. **Update atômico direto no SQL** (mais simples, geralmente melhor para soma de balance):
   ```sql
   UPDATE accounts SET balance = balance + :amount WHERE id = :id
   ```
   Sem `SELECT` + lock explícito — o banco resolve a atomicidade internamente. Contenção ainda existe na mesma linha, mas dura só o `UPDATE`, não a transação inteira.

2. **Optimistic lock** (`@Version` no JPA) — lê sem travar; ao salvar, compara a versão lida com a atual. Se mudou, lança `OptimisticLockException` e quem chamou precisa tentar de novo. Bom quando conflito é raro.

3. **Ledger / event sourcing** — em vez de mutar `balance` diretamente, grava cada movimentação como uma linha imutável (append-only) e calcula o balance como soma das entradas (ou via snapshot periódico). Elimina a contenção de escrita numa única linha; leitura do balance fica mais cara (ou usa materialized view).

4. **Fila serializada por account** — todas as alterações de uma account passam por uma fila/partição única (ex: Kafka particionado por `account_id`, ou actor model). Ordem garantida sem lock de banco, processamento single-threaded por account.

5. **`SELECT FOR UPDATE SKIP LOCKED`** — não se aplica a este caso (soma precisa ser sequencial), mas é útil quando o cenário é fila de itens independentes em vez de acúmulo num mesmo valor.

Para este projeto (poucas escritas concorrentes por account, correção mais importante que throughput extremo), o pessimistic lock atual é razoável. Se a contenção virar problema real, a troca mais simples e de menor risco é o update atômico (opção 1).

---

Imagine que eu tenha um monte de transaçoes ocorrendo numa mesma account, e varias dessas transaçoes
querendo adicionar, mudar o valor do balance da conta. Para garantir que ninguem ta mudando o valor do balance simultaneamente e
nao dar um erro de calculo, ou seja, podemos ter um problemas de condiçao de corrida(race condition), alteraçoes concorrentes,
entao podemos bloquear(lock) a escrita desse valor enquanto o balance esta sendo adicionado.