package com.gpayments.gateway.domain

//  Value Object(VO)
data class CreditCard(
    val number: String,
    val cvv: String,
    val expiryMonth: Int,
    val expiryYear: Int,
    val cardholderName: String,
)
