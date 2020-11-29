package org.tormozzg.microservices.encoding

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.eureka.EnableEurekaClient

@SpringBootApplication
@EnableEurekaClient
class EncodingServerApplication

fun main(args: Array<String>) {
  runApplication<EncodingServerApplication>(*args)
}