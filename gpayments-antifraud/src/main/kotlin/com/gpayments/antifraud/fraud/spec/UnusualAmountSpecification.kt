package com.gpayments.antifraud.fraud.spec

import com.gpayments.antifraud.fraud.FraudReason
import com.gpayments.antifraud.fraud.config.FraudProperties
import com.gpayments.antifraud.invoice.InvoiceRepository
import org.springframework.core.annotation.Order
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component

@Order(3)
@Component
class UnusualAmountSpecification(
    private val invoiceRepository: InvoiceRepository,
    private val fraudProperties: FraudProperties,
) : FraudSpecification {

    override fun detectFraud(context: FraudSpecificationContext): FraudDetectionResult {
        val previousInvoices = invoiceRepository.findByAccountIdOrderByCreatedAtDesc(
            context.account.id,
            PageRequest.of(0, fraudProperties.invoicesHistoryCount),
        )

        if (previousInvoices.isNotEmpty()) {
            val totalAmount = previousInvoices.sumOf { it.amount }
            val averageAmount = totalAmount / previousInvoices.size

            if (context.amount > averageAmount * (1 + fraudProperties.suspiciousVariationPercentage / 100)) {
                val percentageAbove = (context.amount / averageAmount) * 100 - 100
                return FraudDetectionResult(
                    hasFraud = true,
                    reason = FraudReason.UNUSUAL_PATTERN,
                    description = "Amount ${context.amount} is ${"%.2f".format(percentageAbove)}% " +
                        "higher than account average of ${"%.2f".format(averageAmount)}",
                )
            }
        }

        return FraudDetectionResult.noFraud()
    }
}
