package com.gpayments.antifraud.fraud.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "antifraud")
data class FraudProperties(
    val suspiciousVariationPercentage: Double,
    val invoicesHistoryCount: Int,
    val suspiciousInvoicesCount: Int,
    val suspiciousTimeframeHours: Long,
)
