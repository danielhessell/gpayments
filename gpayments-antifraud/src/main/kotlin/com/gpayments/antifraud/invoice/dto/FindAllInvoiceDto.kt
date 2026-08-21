package com.gpayments.antifraud.invoice.dto

data class FindAllInvoiceFilter(
    val withFraud: Boolean? = null,
    val accountId: String? = null,
)
