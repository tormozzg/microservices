package org.tormozzg.microservices.encoding.api.http

import org.tormozzg.microservices.encoding.api.TaskStatus

data class ListTasksStatuses(
    val count: Int,
    val items: Collection<TaskStatus>
)