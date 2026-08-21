package com.gpayments.antifraud.fraud.spec

import com.gpayments.antifraud.account.AccountRepository
import com.gpayments.antifraud.fraud.FraudReason
import com.gpayments.antifraud.fraud.config.FraudProperties
import com.gpayments.antifraud.invoice.InvoiceRepository
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Order(1)
@Component
class FrequentHighValueSpecification(
    private val invoiceRepository: InvoiceRepository,
    private val accountRepository: AccountRepository,
    private val fraudProperties: FraudProperties,
) : FraudSpecification {

    override fun detectFraud(context: FraudSpecificationContext): FraudDetectionResult {
        val recentDate = LocalDateTime.now().minusHours(fraudProperties.suspiciousTimeframeHours)

        val recentInvoices = invoiceRepository.findByAccountIdAndCreatedAtGreaterThanEqual(
            context.account.id,
            recentDate,
        )

        if (recentInvoices.size >= fraudProperties.suspiciousInvoicesCount) {
            context.account.isSuspicious = true
            accountRepository.save(context.account)

            return FraudDetectionResult(
                hasFraud = true,
                reason = FraudReason.FREQUENT_HIGH_VALUE,
                description = "${recentInvoices.size} high-value invoices in the last " +
                    "${fraudProperties.suspiciousTimeframeHours} hours",
            )
        }

        return FraudDetectionResult.noFraud()
    }
}
