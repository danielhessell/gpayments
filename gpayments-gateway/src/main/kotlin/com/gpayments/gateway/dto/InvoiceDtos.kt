package com.gpayments.gateway.dto

import com.gpayments.gateway.domain.Invoice
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import java.math.BigDecimal
import java.time.Instant

data class CreateInvoiceInput(
    @field:DecimalMin(value = "0.01") val amount: BigDecimal,
    @field:NotBlank val description: String,
    @field:NotBlank val paymentType: String,
    @field:NotBlank val cardNumber: String,
    @field:NotBlank val cvv: String,
    val expiryMonth: Int,
    val expiryYear: Int,
    @field:NotBlank val cardholderName: String,
)

data class InvoiceOutput(
    val id: String,
    val accountId: String,
    val amount: BigDecimal,
    val status: String,
    val description: String,
    val paymentType: String,
    val cardLastDigits: String,
    val createdAt: Instant,
    val updatedAt: Instant,
)

fun Invoice.toOutput() = InvoiceOutput(
    id = id,
    accountId = accountId,
    amount = amount,
    status = status.value,
    description = description,
    paymentType = paymentType,
    cardLastDigits = cardLastDigits,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
