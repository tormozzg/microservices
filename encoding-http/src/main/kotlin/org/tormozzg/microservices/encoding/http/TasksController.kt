package org.tormozzg.microservices.encoding.http

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.tormozzg.microservices.encoding.api.Task
import org.tormozzg.microservices.encoding.api.TaskStatus
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/tasks", produces = [MediaType.APPLICATION_JSON_VALUE])
class TasksController(
  private val tasksService: TasksService
) {

  @PostMapping("", consumes = [MediaType.APPLICATION_JSON_VALUE])
  fun create(@RequestBody taskMono: Mono<Task>): Mono<TaskStatus> =
    taskMono
      .flatMap {
        tasksService.createTask(it)
      }
}