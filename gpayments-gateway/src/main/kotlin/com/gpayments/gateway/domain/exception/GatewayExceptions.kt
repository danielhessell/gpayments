package com.gpayments.gateway.domain.exception

sealed class GatewayException(message: String) : RuntimeException(message)

class AccountNotFoundException : GatewayException("account not found")
class DuplicatedApiKeyException : GatewayException("api key already exists")
class InvoiceNotFoundException : GatewayException("invoice not found")
class UnauthorizedAccessException : GatewayException("unauthorized access")
class InvalidAmountException : GatewayException("invalid amount")
class InvalidStatusException : GatewayException("invalid status transition")
class ApiKeyRequiredException : GatewayException("X-API-KEY is required")
