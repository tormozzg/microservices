package org.tormozzg.microservices.encoding.http

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.tormozzg.microservices.encoding.api.Status
import org.tormozzg.microservices.encoding.api.http.ListTasksStatuses
import reactor.core.publisher.Mono

@Service
class TasksService(
    private val repository: TasksRepository
) {

  private val log: Logger = LoggerFactory.getLogger(this::class.java)

  fun fetchList(status: Status? = null, amount: Int = 20, offset: Int = 0): Mono<ListTasksStatuses> = repository.fetchList(status, amount, offset)

}