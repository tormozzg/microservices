package org.tormozzg.microservices.encoding.api

data class Result(
    val id: String,
    val hasResult: Boolean,
    val downloadUrl: String? = null
) {
  companion object {
    fun success(id: String, downloadUrl: String) = Result(id, true, downloadUrl)
    fun error(id: String) = Result(id, false)
  }
}