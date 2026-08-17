package com.gpayments.gateway.kafka.events

import com.gpayments.gateway.domain.Status

data class TransactionResult(
    val invoiceId: String,
    val status: String,
) {
    fun toDomainStatus(): Status = Status.fromValue(status)
}
