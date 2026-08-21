package com.gpayments.antifraud.invoice

import com.gpayments.antifraud.invoice.dto.FindAllInvoiceFilter
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("invoices")
class InvoiceController(
    private val invoiceService: InvoiceService,
) {

    @GetMapping
    fun findAll(
        @RequestParam("with_fraud", required = false) withFraud: Boolean?,
        @RequestParam("account_id", required = false) accountId: String?,
    ): List<Invoice> {
        return invoiceService.findAll(FindAllInvoiceFilter(withFraud, accountId))
    }

    @GetMapping("{id}")
    fun findOne(@PathVariable id: String): Invoice? {
        return invoiceService.findOne(id)
    }
}
