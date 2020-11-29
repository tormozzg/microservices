package org.tormozzg.microservices.encoding.api

data class Task(
    val id: String,
    val resolution: Resolution,
    val validateUrl: String,
    val callbackUrl: String,
    val options: Options
)