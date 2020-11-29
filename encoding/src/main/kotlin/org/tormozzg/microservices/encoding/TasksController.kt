package org.tormozzg.microservices.encoding

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.tormozzg.microservices.encoding.api.Task
import org.tormozzg.microservices.encoding.api.TaskStatus
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/tasks", produces = [MediaType.APPLICATION_JSON_VALUE])
class TasksController(
    private val encodingService: EncodingService
) {

  private val log: Logger = LoggerFactory.getLogger(this::class.java)

  @PostMapping("/", consumes = [MediaType.APPLICATION_JSON_VALUE])
  @ResponseStatus(HttpStatus.ACCEPTED)
  fun create(@RequestBody taskMono: Mono<Task>): Mono<Any> =
      taskMono.flatMap { task ->
        log.debug("New Incoming request. #{}", task.id)
        encodingService.tryToAcceptTask(task)
      }


  @GetMapping("/{id}")
  fun status(@PathVariable id: String): Mono<TaskStatus>  =
      encodingService.getTaskStatus(id)


}