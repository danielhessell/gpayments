package com.gpayments.antifraud.invoice.kafka

import com.fasterxml.jackson.annotation.JsonProperty

data class PendingInvoicesMessage(
    @JsonProperty("account_id") val accountId: String,
    @JsonProperty("amount") val amount: Double,
    @JsonProperty("invoice_id") val invoiceId: String,
)
