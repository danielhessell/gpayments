package com.gpayments.antifraud.fraud.spec

import com.gpayments.antifraud.account.Account
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import com.gpayments.antifraud.fraud.FraudReason

class FraudAggregateSpecificationTest {

    @Test
    fun `returns first matching specification result in order`() {
        val first = object : FraudSpecification {
            override fun detectFraud(context: FraudSpecificationContext) =
                FraudDetectionResult(hasFraud = true, reason = FraudReason.FREQUENT_HIGH_VALUE)
        }
        val second = object : FraudSpecification {
            override fun detectFraud(context: FraudSpecificationContext) =
                FraudDetectionResult(hasFraud = true, reason = FraudReason.SUSPICIOUS_ACCOUNT)
        }

        val aggregate = FraudAggregateSpecification(listOf(first, second))
        val result = aggregate.detectFraud(
            FraudSpecificationContext(account = Account(id = "acc-1"), amount = 100.0, invoiceId = "inv-1"),
        )

        assertEquals(FraudReason.FREQUENT_HIGH_VALUE, result.reason)
    }

    @Test
    fun `returns no fraud when no specification matches`() {
        val spec = object : FraudSpecification {
            override fun detectFraud(context: FraudSpecificationContext) = FraudDetectionResult.noFraud()
        }

        val aggregate = FraudAggregateSpecification(listOf(spec))
        val result = aggregate.detectFraud(
            FraudSpecificationContext(account = Account(id = "acc-1"), amount = 100.0, invoiceId = "inv-1"),
        )

        assertFalse(result.hasFraud)
    }
}
