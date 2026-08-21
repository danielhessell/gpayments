package com.gpayments.antifraud.invoice.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import com.gpayments.antifraud.fraud.FraudService
import com.gpayments.antifraud.fraud.ProcessInvoiceFraudDto
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class InvoicesConsumer(
    private val fraudService: FraudService,
    private val objectMapper: ObjectMapper,
) {
    private val logger = LoggerFactory.getLogger(InvoicesConsumer::class.java)

    @KafkaListener(topics = ["\${app.kafka.pending-topic:pending_transactions}"])
    fun handlePendingInvoices(payload: String) {
        val message = objectMapper.readValue(payload, PendingInvoicesMessage::class.java)

        logger.info("Processing invoice: {}", message.invoiceId)
        fraudService.processInvoice(
            ProcessInvoiceFraudDto(
                invoiceId = message.invoiceId,
                accountId = message.accountId,
                amount = message.amount,
            ),
        )
        logger.info("Invoice processed: {}", message.invoiceId)
    }
}
