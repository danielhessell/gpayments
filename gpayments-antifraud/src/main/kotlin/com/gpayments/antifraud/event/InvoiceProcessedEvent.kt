package com.gpayments.antifraud.event

import com.gpayments.antifraud.fraud.spec.FraudDetectionResult
import com.gpayments.antifraud.invoice.Invoice

data class InvoiceProcessedEvent(
    val invoice: Invoice,
    val fraudResult: FraudDetectionResult,
)
