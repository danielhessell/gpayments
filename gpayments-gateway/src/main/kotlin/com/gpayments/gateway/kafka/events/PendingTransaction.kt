package com.gpayments.gateway.kafka.events

import java.math.BigDecimal

data class PendingTransaction(
    val accountId: String,
    val invoiceId: String,
    val amount: BigDecimal,
)
