package com.gpayments.antifraud

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class AntifraudApplication

fun main(args: Array<String>) {
    runApplication<AntifraudApplication>(*args)
}
