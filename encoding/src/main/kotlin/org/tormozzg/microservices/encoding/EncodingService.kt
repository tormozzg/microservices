package org.tormozzg.microservices.encoding

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.tormozzg.microservices.encoding.api.Status
import org.tormozzg.microservices.encoding.api.Task
import org.tormozzg.microservices.encoding.api.TaskCallback
import org.tormozzg.microservices.encoding.jobs.PipelineFactory
import reactor.core.publisher.Mono
import javax.annotation.PostConstruct

@Service
class EncodingService(
    private val om: ObjectMapper,
    private val pipelineFactory: PipelineFactory,
    private val webClientBuilder: WebClient.Builder
) {

    lateinit var encodingHttpClient: WebClient

    @PostConstruct
    fun init() {
        encodingHttpClient = webClientBuilder.baseUrl("http://encoding-http").build()
    }

    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    private fun sendCallback(id: String, status: Status) {
        encodingHttpClient
            .post()
            .uri("tasks/callback")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TaskCallback(id, status, "Service"))
            .exchange()
            .subscribe({
                if (it.statusCode().is2xxSuccessful) {
                    log.debug("Callback of task #{} has been successfully sent", id)
                } else {
                    log.warn("Could not send task callback. Invalid response status code {}", it.rawStatusCode())
                }
            }, { e ->
                log.error("Error occurred while request encoding http service to send callback.", e)
            })
    }

    @KafkaListener(topics = ["encoding-tasks"])
    fun onNewTask(@Payload data: ByteArray, acknowledgment: Acknowledgment) {
        val task = om.readValue(data, Task::class.java)
        try {
            sendCallback(task.id, Status.RUNNING)
            pipelineFactory.createPipeline(task).run()
            acknowledgment.acknowledge()
            sendCallback(task.id, Status.DONE)
        } catch (e: Exception) {
            log.error("Error occurred during executing task #{}", task.id, e)
            sendCallback(task.id, Status.ERROR)
        }
    }
}