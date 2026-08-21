package com.gpayments.antifraud.invoice

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface InvoiceRepository : JpaRepository<Invoice, String> {

    @Query(
        """
        select i from Invoice i
        where (:accountId is null or i.account.id = :accountId)
        and (:status is null or i.status = :status)
        """
    )
    fun findAllByFilter(
        @Param("accountId") accountId: String?,
        @Param("status") status: InvoiceStatus?,
    ): List<Invoice>

    fun findByAccountIdAndCreatedAtGreaterThanEqual(
        accountId: String,
        createdAt: LocalDateTime,
    ): List<Invoice>

    fun findByAccountIdOrderByCreatedAtDesc(accountId: String, pageable: Pageable): List<Invoice>
}
