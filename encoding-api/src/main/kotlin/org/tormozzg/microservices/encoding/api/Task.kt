package org.tormozzg.microservices.encoding.api

data class Task(
    val id: String,
    val type: TaskType,
    val resolution: Resolution,
    val fileUrl: String,
    val validateUrl: String,
    val callbackUrl: String,
    val options: Options?
)

enum class TaskType {
    ENCODE, PREVIEW
}