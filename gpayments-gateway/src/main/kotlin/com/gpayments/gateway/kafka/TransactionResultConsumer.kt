package com.gpayments.gateway.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import com.gpayments.gateway.kafka.events.TransactionResult
import com.gpayments.gateway.service.InvoiceService
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class TransactionResultConsumer(
    private val invoiceService: InvoiceService,
    private val objectMapper: ObjectMapper,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(
        topics = ["\${app.kafka.consumer-topic}"],
        groupId = "\${app.kafka.consumer-group-id}",
    )
    fun consume(message: String) {
        val result = objectMapper.readValue(message, TransactionResult::class.java)
        try {
            invoiceService.processTransactionResult(result.invoiceId, result.toDomainStatus())
            log.info("transaction result processed invoiceId={} status={}", result.invoiceId, result.status)
        } catch (ex: Exception) {
            log.error("error processing transaction result invoiceId={} status={}", result.invoiceId, result.status, ex)
        }
    }
}
