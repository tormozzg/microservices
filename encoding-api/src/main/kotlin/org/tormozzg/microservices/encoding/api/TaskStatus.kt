package org.tormozzg.microservices.encoding.api

data class TaskStatus(val id: String,
                      val status: Status)

enum class Status {
  IN_QUEUE, RUNNING, ERROR, DONE
}