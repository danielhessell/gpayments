package com.gpayments.antifraud.fraud.spec

import com.gpayments.antifraud.fraud.FraudReason
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Order(2)
@Component
class SuspiciousAccountSpecification : FraudSpecification {

    override fun detectFraud(context: FraudSpecificationContext): FraudDetectionResult {
        if (context.account.isSuspicious) {
            return FraudDetectionResult(
                hasFraud = true,
                reason = FraudReason.SUSPICIOUS_ACCOUNT,
                description = "Account is flagged as suspicious",
            )
        }

        return FraudDetectionResult.noFraud()
    }
}
