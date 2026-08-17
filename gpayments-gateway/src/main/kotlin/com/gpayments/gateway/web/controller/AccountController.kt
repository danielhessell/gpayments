package com.gpayments.gateway.web.controller

import com.gpayments.gateway.domain.exception.ApiKeyRequiredException
import com.gpayments.gateway.dto.AccountOutput
import com.gpayments.gateway.dto.CreateAccountInput
import com.gpayments.gateway.service.AccountService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/accounts")
class AccountController(private val accountService: AccountService) {

    @PostMapping
    fun create(@Valid @RequestBody input: CreateAccountInput): ResponseEntity<AccountOutput> =
        ResponseEntity.status(HttpStatus.CREATED).body(accountService.createAccount(input))

    @GetMapping
    fun get(@RequestHeader("X-API-Key", required = false) apiKey: String?): ResponseEntity<AccountOutput> {
        if (apiKey.isNullOrBlank()) throw ApiKeyRequiredException()
        return ResponseEntity.ok(accountService.findByApiKey(apiKey))
    }
}
