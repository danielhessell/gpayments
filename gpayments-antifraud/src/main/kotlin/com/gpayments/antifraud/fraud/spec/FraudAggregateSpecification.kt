package com.gpayments.antifraud.fraud.spec

import org.springframework.stereotype.Component

@Component
class FraudAggregateSpecification(
    private val specifications: List<FraudSpecification>,
) : FraudSpecification {

    override fun detectFraud(context: FraudSpecificationContext): FraudDetectionResult {
        for (specification in specifications) {
            val result = specification.detectFraud(context)
            if (result.hasFraud) {
                return result
            }
        }

        return FraudDetectionResult.noFraud()
    }
}
