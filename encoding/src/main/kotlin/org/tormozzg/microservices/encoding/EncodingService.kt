package org.tormozzg.microservices.encoding

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.tormozzg.microservices.encoding.api.Task
import org.tormozzg.microservices.encoding.api.TaskStatus
import reactor.core.publisher.Mono

@Service
class EncodingService {

  private val log: Logger = LoggerFactory.getLogger(this::class.java)

  fun tryToAcceptTask(task: Task): Mono<out Any> {
    TODO("Not yet implemented")
  }

  fun getTaskStatus(id: String): Mono<TaskStatus> {
    TODO("Not yet implemented")
  }
}