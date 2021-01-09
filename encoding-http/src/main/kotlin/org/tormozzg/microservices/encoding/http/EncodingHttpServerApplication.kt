package org.tormozzg.microservices.encoding.http

import com.netflix.hystrix.HystrixCommandGroupKey
import com.netflix.hystrix.HystrixCommandProperties
import com.netflix.hystrix.HystrixObservableCommand
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.circuitbreaker.Customizer
import org.springframework.cloud.netflix.eureka.EnableEurekaClient
import org.springframework.cloud.netflix.hystrix.ReactiveHystrixCircuitBreakerFactory
import org.springframework.context.annotation.Bean


@SpringBootApplication
@EnableEurekaClient
class EncodingHttpServerApplication{
  @Bean
  fun defaultConfig(): Customizer<ReactiveHystrixCircuitBreakerFactory> {
    return Customizer { factory ->
      factory.configureDefault { id ->
        HystrixObservableCommand.Setter
            .withGroupKey(HystrixCommandGroupKey.Factory.asKey(id))
            .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                .withExecutionTimeoutInMilliseconds(4000))
      }
    }
  }
}

fun main(args: Array<String>) {
  runApplication<EncodingHttpServerApplication>(*args)
}