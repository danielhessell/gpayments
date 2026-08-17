package com.gpayments.gateway.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.security.SecureRandom
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "accounts")
class Account(
    @Id
    val id: String = UUID.randomUUID().toString(),

    val name: String,

    val email: String,

    @Column(name = "api_key")
    val apiKey: String = generateApiKey(),

    var balance: BigDecimal = BigDecimal.ZERO,

    @Column(name = "created_at")
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at")
    var updatedAt: Instant = Instant.now(),
) {
    fun addBalance(amount: BigDecimal) {
        balance = balance.add(amount)
        updatedAt = Instant.now()
    }

    companion object {
        fun newAccount(name: String, email: String): Account = Account(name = name, email = email)

        private fun generateApiKey(): String {
            val bytes = ByteArray(16)
            SecureRandom().nextBytes(bytes)
            return bytes.joinToString("") { "%02x".format(it) }
        }
    }
}
