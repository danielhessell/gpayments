package com.gpayments.gateway.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import com.gpayments.gateway.kafka.events.PendingTransaction
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class PendingTransactionProducer(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper,
    @Value("\${app.kafka.producer-topic}") private val topic: String,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun send(event: PendingTransaction) {
        val payload = objectMapper.writeValueAsString(event)
        log.info("sending pending transaction to kafka topic={} invoiceId={}", topic, event.invoiceId)
        kafkaTemplate.send(topic, event.invoiceId, payload)
    }
}
