package com.gpayments.gateway.repository

import com.gpayments.gateway.domain.Invoice
import org.springframework.data.jpa.repository.JpaRepository

interface InvoiceRepository : JpaRepository<Invoice, String> {
    fun findByAccountId(accountId: String): List<Invoice>
}
