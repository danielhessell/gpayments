package com.gpayments.antifraud.fraud

import com.gpayments.antifraud.account.Account
import com.gpayments.antifraud.account.AccountRepository
import com.gpayments.antifraud.event.InvoiceProcessedEvent
import com.gpayments.antifraud.fraud.spec.FraudAggregateSpecification
import com.gpayments.antifraud.fraud.spec.FraudDetectionResult
import com.gpayments.antifraud.fraud.spec.FraudSpecificationContext
import com.gpayments.antifraud.invoice.Invoice
import com.gpayments.antifraud.invoice.InvoiceRepository
import com.gpayments.antifraud.invoice.InvoiceStatus
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

data class ProcessInvoiceResult(
    val invoice: Invoice,
    val fraudResult: FraudDetectionResult,
)

@Service
class FraudService(
    private val invoiceRepository: InvoiceRepository,
    private val accountRepository: AccountRepository,
    private val fraudAggregateSpec: FraudAggregateSpecification,
    private val eventPublisher: ApplicationEventPublisher,
) {

    @Transactional
    fun processInvoice(dto: ProcessInvoiceFraudDto): ProcessInvoiceResult {
        if (invoiceRepository.existsById(dto.invoiceId)) {
            error("Invoice has already been processed")
        }

        val account = accountRepository.findById(dto.accountId)
            .orElseGet { accountRepository.save(Account(id = dto.accountId)) }

        val fraudResult = fraudAggregateSpec.detectFraud(
            FraudSpecificationContext(
                account = account,
                amount = dto.amount,
                invoiceId = dto.invoiceId,
            ),
        )

        val invoice = Invoice(
            id = dto.invoiceId,
            account = account,
            amount = dto.amount,
            status = if (fraudResult.hasFraud) InvoiceStatus.REJECTED else InvoiceStatus.APPROVED,
        )

        if (fraudResult.hasFraud) {
            invoice.fraudHistory = FraudHistory(
                invoice = invoice,
                reason = fraudResult.reason!!,
                description = fraudResult.description,
            )
        }

        invoiceRepository.save(invoice)

        eventPublisher.publishEvent(InvoiceProcessedEvent(invoice, fraudResult))

        return ProcessInvoiceResult(invoice, fraudResult)
    }
}
