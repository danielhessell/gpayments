package com.gpayments.gateway.web.filter

import com.gpayments.gateway.domain.exception.ApiKeyRequiredException
import com.gpayments.gateway.service.AccountService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class ApiKeyAuthInterceptor(private val accountService: AccountService) : HandlerInterceptor {

    companion object {
        const val API_KEY_ATTR = "authenticatedApiKey"
    }

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val apiKey = request.getHeader("X-API-KEY")
        if (apiKey.isNullOrBlank()) throw ApiKeyRequiredException()
        accountService.findAccountByApiKey(apiKey)
        request.setAttribute(API_KEY_ATTR, apiKey)
        return true
    }
}
