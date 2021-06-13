package org.tormozzg.microservices.encoding.http

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.tormozzg.microservices.encoding.api.Task
import org.tormozzg.microservices.encoding.api.TaskCallback
import org.tormozzg.microservices.encoding.api.TaskStatus
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/tasks", produces = [MediaType.APPLICATION_JSON_VALUE])
class TasksController(
    private val tasksService: TasksService
) {

    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    @PostMapping("", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun create(@RequestBody taskMono: Mono<Task>): Mono<TaskStatus> =
        taskMono
            .flatMap {
                tasksService.createTask(it)
            }

    @PostMapping("/callback", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun status(@RequestBody callbackMono: Mono<TaskCallback>): Mono<ResponseEntity<Any>> =
        callbackMono.map { task ->
            log.info("Got callback for {}. Status {}", task.id, task.status)
            ResponseEntity.noContent().build()
        }
}