package com.gpayments.gateway.web.controller

import com.gpayments.gateway.dto.CreateInvoiceInput
import com.gpayments.gateway.dto.InvoiceOutput
import com.gpayments.gateway.service.InvoiceService
import com.gpayments.gateway.web.filter.ApiKeyAuthInterceptor.Companion.API_KEY_ATTR
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/invoice")
class InvoiceController(private val invoiceService: InvoiceService) {

    @PostMapping
    fun create(@Valid @RequestBody input: CreateInvoiceInput, request: HttpServletRequest): ResponseEntity<InvoiceOutput> =
        ResponseEntity.status(HttpStatus.CREATED).body(invoiceService.create(input, apiKey(request)))

    @GetMapping("/{id}")
    fun getById(@PathVariable id: String, request: HttpServletRequest): ResponseEntity<InvoiceOutput> =
        ResponseEntity.ok(invoiceService.getById(id, apiKey(request)))

    @GetMapping
    fun list(request: HttpServletRequest): ResponseEntity<List<InvoiceOutput>> =
        ResponseEntity.ok(invoiceService.listByAccountApiKey(apiKey(request)))

    private fun apiKey(request: HttpServletRequest): String = request.getAttribute(API_KEY_ATTR) as String
}
