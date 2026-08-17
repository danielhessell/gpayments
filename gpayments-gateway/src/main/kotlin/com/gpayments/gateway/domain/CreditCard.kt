package com.gpayments.gateway.domain

data class CreditCard(
    val number: String,
    val cvv: String,
    val expiryMonth: Int,
    val expiryYear: Int,
    val cardholderName: String,
)
