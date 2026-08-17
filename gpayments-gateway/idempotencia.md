Consumer Kafka (transaction_results) é idempotente, por construção, não por design explícito:

Invoice.updateStatus() em domain/Invoice.kt:58 só permite transição a partir de PENDING:
if (status != Status.PENDING) throw InvalidStatusException()
Kafka é at-least-once — mesma mensagem pode chegar 2x (rebalance, retry, etc). Reprocessar {invoice_id, status:"approved"} pra invoice já approved: guard rejeita, TransactionResultConsumer.kt:26-27 captura exceção, loga, descarta. Balance não é incrementado de novo. Reprocessamento seguro.

Resto da aplicação NÃO é idempotente:

- POST /accounts — sem idempotency key. Retry com mesmo payload → nova conta, novo UUID, nova api_key. Único guard é colisão de api_key aleatória (nunca colide na prática) — email é UNIQUE no schema mas sem @ExceptionHandler pra DataIntegrityViolationException, então retry com mesmo email quebra com 500 internal server error, não com resposta idempotente/consistente.
- POST /invoice — sem idempotency key. Retry (timeout de rede, double-click) → invoice duplicada, e se aprovada, balance duplicado. Client não tem como distinguir "minha invoice já foi criada" de "falhou, tenta de novo".
- Kafka producer — sem enable.idempotence/acks configurado em KafkaConfig.kt (só bootstrap+serializers). Retry de rede no producer pode duplicar mensagem em pending_transactions — mitigado só porque o consumer do lado antifraude (fora deste projeto) teria que lidar com isso, e aqui o updateStatus guard cobre o retorno.