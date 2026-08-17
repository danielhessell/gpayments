package com.gpayments.gateway.service

import com.gpayments.gateway.domain.CreditCard
import com.gpayments.gateway.domain.Invoice
import com.gpayments.gateway.domain.Status
import com.gpayments.gateway.domain.exception.InvoiceNotFoundException
import com.gpayments.gateway.domain.exception.UnauthorizedAccessException
import com.gpayments.gateway.dto.CreateInvoiceInput
import com.gpayments.gateway.dto.InvoiceOutput
import com.gpayments.gateway.dto.toOutput
import com.gpayments.gateway.kafka.PendingTransactionProducer
import com.gpayments.gateway.kafka.events.PendingTransaction
import com.gpayments.gateway.repository.InvoiceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class InvoiceService(
    private val invoiceRepository: InvoiceRepository,
    private val accountService: AccountService,
    private val pendingTransactionProducer: PendingTransactionProducer,
) {
    fun create(input: CreateInvoiceInput, apiKey: String): InvoiceOutput {
        val account = accountService.findByApiKey(apiKey)
        val card = CreditCard(
            number = input.cardNumber,
            cvv = input.cvv,
            expiryMonth = input.expiryMonth,
            expiryYear = input.expiryYear,
            cardholderName = input.cardholderName,
        )
        val invoice = Invoice.newInvoice(account.id, input.amount, input.description, input.paymentType, card)
        invoice.process()

        if (invoice.status == Status.PENDING) {
            pendingTransactionProducer.send(PendingTransaction(invoice.accountId, invoice.id, invoice.amount))
        }
        invoiceRepository.save(invoice)
        if (invoice.status == Status.APPROVED) {
            accountService.updateBalance(apiKey, invoice.amount)
        }
        return invoice.toOutput()
    }

    fun getById(id: String, apiKey: String): InvoiceOutput {
        val invoice = invoiceRepository.findById(id).orElse(null) ?: throw InvoiceNotFoundException()
        val account = accountService.findByApiKey(apiKey)
        if (invoice.accountId != account.id) throw UnauthorizedAccessException()
        return invoice.toOutput()
    }

    fun listByAccountApiKey(apiKey: String): List<InvoiceOutput> {
        val account = accountService.findByApiKey(apiKey)
        return invoiceRepository.findByAccountId(account.id).map { it.toOutput() }
    }

    @Transactional
    fun processTransactionResult(invoiceId: String, status: Status) {
        val invoice = invoiceRepository.findById(invoiceId).orElse(null) ?: throw InvoiceNotFoundException()
        invoice.updateStatus(status)
        invoiceRepository.save(invoice)
        if (status == Status.APPROVED) {
            val account = accountService.findById(invoice.accountId)
            accountService.updateBalance(account.apiKey, invoice.amount)
        }
    }
}
