package com.gpayments.gateway.config

import com.gpayments.gateway.web.filter.ApiKeyAuthInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebMvcConfig(private val apiKeyAuthInterceptor: ApiKeyAuthInterceptor) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(apiKeyAuthInterceptor).addPathPatterns("/invoice/**")
    }
}
