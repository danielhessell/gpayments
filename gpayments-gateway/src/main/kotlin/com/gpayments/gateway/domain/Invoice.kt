package com.gpayments.gateway.domain

import com.gpayments.gateway.domain.exception.InvalidAmountException
import com.gpayments.gateway.domain.exception.InvalidStatusException
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ThreadLocalRandom

private val PENDING_THRESHOLD: BigDecimal = BigDecimal(10000) //  VALOR SUSPEITO
private const val APPROVAL_RATE = 0.7 // TAXA DE APROVAÇAO

@Entity
@Table(name = "invoices")
class Invoice(
    @Id
    val id: String = UUID.randomUUID().toString(),

    @Column(name = "account_id")
    val accountId: String,

    val amount: BigDecimal,

    @Enumerated(EnumType.STRING)
    var status: Status = Status.PENDING,

    val description: String,

    @Column(name = "payment_type")
    val paymentType: String,

    @Column(name = "card_last_digits")
    val cardLastDigits: String,

    @Column(name = "created_at")
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at")
    var updatedAt: Instant = Instant.now(),
) {
    fun process() {
        if (amount > PENDING_THRESHOLD) return
        // SIMULANDO TAXA DE APROVAÇAO
        status = if (ThreadLocalRandom.current().nextDouble() <= APPROVAL_RATE) {
            Status.APPROVED
        } else {
            Status.REJECTED
        }
        updatedAt = Instant.now()
    }

    fun updateStatus(newStatus: Status) {
        if (status != Status.PENDING) throw InvalidStatusException()
        status = newStatus
        updatedAt = Instant.now()
    }

    companion object {
        fun newInvoice(
            accountId: String,
            amount: BigDecimal,
            description: String,
            paymentType: String,
            card: CreditCard,
        ): Invoice {
            if (amount <= BigDecimal.ZERO) throw InvalidAmountException()
            val last4 = if (card.number.length >= 4) card.number.takeLast(4) else card.number
            return Invoice(
                accountId = accountId,
                amount = amount,
                description = description,
                paymentType = paymentType,
                cardLastDigits = last4,
            )
        }
    }
}
