package com.gpayments.gateway.domain

enum class Status(val value: String) {
    PENDING("pending"),
    APPROVED("approved"),
    REJECTED("rejected");

    companion object {
        fun fromValue(value: String): Status = entries.first { it.value == value }
    }
}
