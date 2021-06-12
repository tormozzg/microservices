package org.tormozzg.microservices.encoding

import org.slf4j.MDC
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.cloud.netflix.eureka.EnableEurekaClient
import org.tormozzg.microservices.encoding.jobs.Artifacts

@SpringBootApplication
@EnableEurekaClient
@EnableConfigurationProperties(
    EncodingConfiguration::class
)
class EncodingServerApplication

fun main(args: Array<String>) {
    runApplication<EncodingServerApplication>(*args)
}

inline fun <T> logTags(vararg tags: Pair<String, String>, func: () -> T): T {
    val oldContext = MDC.getCopyOfContextMap()
    try {
        tags.forEach {
            MDC.put(it.first, it.second)
        }
        return func.invoke()
    } finally {
        MDC.setContextMap(oldContext)
    }
}