package org.tormozzg.microservices.encoding.api

data class TaskCallback(
    val id: String,
    val status: Status,
    val service: String
)