package com.gpayments.antifraud.fraud

data class ProcessInvoiceFraudDto(
    val invoiceId: String,
    val accountId: String,
    val amount: Double,
)
