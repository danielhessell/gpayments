package com.gpayments.gateway.dto

import com.gpayments.gateway.domain.Account
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import java.math.BigDecimal
import java.time.Instant

data class CreateAccountInput(
    @field:NotBlank val name: String,
    @field:NotBlank @field:Email val email: String,
)

data class AccountOutput(
    val id: String,
    val name: String,
    val email: String,
    val balance: BigDecimal,
    val apiKey: String,
    val createdAt: Instant,
    val updatedAt: Instant,
)

fun Account.toOutput() = AccountOutput(
    id = id,
    name = name,
    email = email,
    balance = balance,
    apiKey = apiKey,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
