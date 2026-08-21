package com.gpayments.antifraud.event

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.event.EventListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class PublishProcessedInvoiceListener(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val objectMapper: ObjectMapper,
    @Value("\${app.kafka.result-topic:transaction_results}") private val resultTopic: String,
) {
    private val logger = LoggerFactory.getLogger(PublishProcessedInvoiceListener::class.java)

    @EventListener
    fun handle(event: InvoiceProcessedEvent) {
        val payload = objectMapper.writeValueAsString(
            mapOf(
                "invoice_id" to event.invoice.id,
                "status" to if (event.fraudResult.hasFraud) "rejected" else "approved",
            ),
        )

        kafkaTemplate.send(resultTopic, payload)
        logger.info("Invoice {} processed event published", event.invoice.id)
    }
}
