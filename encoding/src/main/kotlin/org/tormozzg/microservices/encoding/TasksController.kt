package org.tormozzg.microservices.encoding

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.tormozzg.microservices.encoding.api.Status
import org.tormozzg.microservices.encoding.api.TaskStatus
import org.tormozzg.microservices.encoding.api.http.ListTasksStatuses
import reactor.core.publisher.Mono
import java.util.*

@RestController
@RequestMapping("/tasks", produces = [MediaType.APPLICATION_JSON_VALUE])
class TasksController(
) {

  private val log: Logger = LoggerFactory.getLogger(this::class.java)

  @GetMapping("")
  fun fetchList(
    @RequestParam status: Status? = null,
    @RequestParam(defaultValue = "20") amount: Int = 20,
    @RequestParam(defaultValue = "0") offset: Int = 0
  ): Mono<ListTasksStatuses> =
    Mono.just(
      ListTasksStatuses(
        0,
        listOf(
          TaskStatus(UUID.randomUUID().toString(), Status.IN_QUEUE),
          TaskStatus(UUID.randomUUID().toString(), Status.RUNNING),
          TaskStatus(UUID.randomUUID().toString(), Status.DONE)
        )
      )
    )
}