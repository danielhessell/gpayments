package com.gpayments.antifraud.fraud.spec

import com.gpayments.antifraud.account.Account
import com.gpayments.antifraud.fraud.FraudReason

data class FraudSpecificationContext(
    val account: Account,
    val amount: Double,
    val invoiceId: String,
)

data class FraudDetectionResult(
    val hasFraud: Boolean,
    val reason: FraudReason? = null,
    val description: String? = null,
) {
    companion object {
        fun noFraud() = FraudDetectionResult(hasFraud = false)
    }
}

interface FraudSpecification {
    fun detectFraud(context: FraudSpecificationContext): FraudDetectionResult
}
