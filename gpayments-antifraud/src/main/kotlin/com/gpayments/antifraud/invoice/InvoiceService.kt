package com.gpayments.antifraud.invoice

import com.gpayments.antifraud.invoice.dto.FindAllInvoiceFilter
import org.springframework.stereotype.Service

@Service
class InvoiceService(
    private val invoiceRepository: InvoiceRepository,
) {

    fun findAll(filter: FindAllInvoiceFilter): List<Invoice> {
        val status = if (filter.withFraud == true) InvoiceStatus.REJECTED else null
        return invoiceRepository.findAllByFilter(filter.accountId, status)
    }

    fun findOne(id: String): Invoice? {
        return invoiceRepository.findById(id).orElse(null)
    }
}
