package org.tormozzg.microservices.encoding

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import org.tormozzg.microservices.encoding.api.Task
import reactor.core.publisher.Mono

@Service
class EncodingService(
  private val om: ObjectMapper
) {

  private val log: Logger = LoggerFactory.getLogger(this::class.java)

  @KafkaListener(topics = ["encoding-tasks"])
  fun onNewTask(@Payload data: ByteArray, acknowledgment: Acknowledgment) {
    Mono.fromCallable {
      om.readValue(data, Task::class.java)
    }
      .map { task ->
        log.info("Got new task #{}", task.id)
        task
      }
      .flatMap {
        Thread.sleep(2500)
        Mono.just(it)
      }
      .subscribe({ task ->
        log.info("Task #{} successfully done", task.id)
        acknowledgment.acknowledge()
      }, {})
  }
}