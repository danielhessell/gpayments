package com.gpayments.gateway.web.exception

import com.gpayments.gateway.domain.exception.AccountNotFoundException
import com.gpayments.gateway.domain.exception.ApiKeyRequiredException
import com.gpayments.gateway.domain.exception.DuplicatedApiKeyException
import com.gpayments.gateway.domain.exception.DuplicatedEmailException
import com.gpayments.gateway.domain.exception.GatewayException
import com.gpayments.gateway.domain.exception.InvalidAmountException
import com.gpayments.gateway.domain.exception.InvalidStatusException
import com.gpayments.gateway.domain.exception.InvoiceNotFoundException
import com.gpayments.gateway.domain.exception.UnauthorizedAccessException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(AccountNotFoundException::class, ApiKeyRequiredException::class)
    fun handleUnauthorized(ex: GatewayException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse(ex.message ?: "unauthorized"))

    @ExceptionHandler(DuplicatedApiKeyException::class, DuplicatedEmailException::class)
    fun handleConflict(ex: GatewayException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse(ex.message ?: "conflict"))

    @ExceptionHandler(InvoiceNotFoundException::class)
    fun handleNotFound(ex: InvoiceNotFoundException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse(ex.message ?: "not found"))

    @ExceptionHandler(UnauthorizedAccessException::class)
    fun handleForbidden(ex: UnauthorizedAccessException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse(ex.message ?: "forbidden"))

    @ExceptionHandler(InvalidAmountException::class, InvalidStatusException::class)
    fun handleBadRequest(ex: GatewayException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse(ex.message ?: "bad request"))

    @ExceptionHandler(MethodArgumentNotValidException::class, HttpMessageNotReadableException::class)
    fun handleValidation(ex: Exception): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse("invalid request"))

    @ExceptionHandler(Exception::class)
    fun handleGeneric(ex: Exception): ResponseEntity<ErrorResponse> {
        log.error("unhandled error", ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponse("internal server error"))
    }
}
