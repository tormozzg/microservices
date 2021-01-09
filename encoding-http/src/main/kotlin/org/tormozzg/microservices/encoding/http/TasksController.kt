package org.tormozzg.microservices.encoding.http

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.tormozzg.microservices.encoding.api.Status
import org.tormozzg.microservices.encoding.api.http.ListTasksStatuses
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/tasks", produces = [MediaType.APPLICATION_JSON_VALUE])
class TasksController(
    private val tasksService: TasksService
) {

  private val log: Logger = LoggerFactory.getLogger(this::class.java)

  @GetMapping("")
  fun fetchList(@RequestParam status: Status? = null,
                @RequestParam(defaultValue = "20") amount: Int = 20,
                @RequestParam(defaultValue = "0") offset: Int = 0): Mono<ListTasksStatuses> = tasksService.fetchList(status, amount, offset)

  @PostMapping("", consumes = [MediaType.APPLICATION_JSON_VALUE])
  fun create(): Mono<Any> {
    TODO()
  }


}