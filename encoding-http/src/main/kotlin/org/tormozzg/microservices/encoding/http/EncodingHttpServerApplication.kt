package org.tormozzg.microservices.encoding.http

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.eureka.EnableEurekaClient


@SpringBootApplication
@EnableEurekaClient
@EnableConfigurationProperties(EncodingServiceConfig::class)
class EncodingHttpServerApplication

fun main(args: Array<String>) {
  runApplication<EncodingHttpServerApplication>(*args)
}