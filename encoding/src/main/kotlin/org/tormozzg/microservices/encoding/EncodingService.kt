package org.tormozzg.microservices.encoding

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriComponentsBuilder
import org.tormozzg.microservices.encoding.api.Result
import org.tormozzg.microservices.encoding.api.Status
import org.tormozzg.microservices.encoding.api.Task
import org.tormozzg.microservices.encoding.api.TaskCallback
import org.tormozzg.microservices.encoding.jobs.PipelineFactory
import org.tormozzg.microservices.encoding.jobs.PipelineResult
import reactor.util.retry.Retry
import javax.annotation.PostConstruct

@Service
class EncodingService(
    private val om: ObjectMapper,
    private val pipelineFactory: PipelineFactory,
    private val webClientBuilder: WebClient.Builder,
    private val config: EncodingConfiguration
) {

    lateinit var encodingHttpClient: WebClient

    private val webClient: WebClient = WebClient.create()

    @PostConstruct
    fun init() {
        encodingHttpClient = webClientBuilder.baseUrl("http://encoding-http").build()
    }

    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    private fun sendTaskStatus(id: String, status: Status) {
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

    private fun sendTaskCallback(result: PipelineResult) {
        val task = result.task
        webClient.post()
            .uri(task.callbackUrl)
            .bodyValue(Result(task.id, result.resultFile != null, getDownloadUrl(result.resultFile!!.name)))
            .exchange()
            .map {
                if (it.statusCode().is2xxSuccessful)
                    it
                else
                    throw IllegalStateException("Could not send callback. Invalid response status code ${it.rawStatusCode()}")
            }
            .retryWhen(Retry.fixedDelay(5, config.resendCallbackDelay))
            .subscribe({
                if (it.statusCode().is2xxSuccessful) {
                    log.debug(
                        "Callback [{}] of task #{} has been successfully sent.",
                        task.callbackUrl,
                        task.id
                    )
                } else {
                    log.warn(
                        "Invalid response status code #{} for callback [{}] of task #{}",
                        it.rawStatusCode(),
                        task.callbackUrl,
                        task.id
                    )
                }
            }, {
                log.error("Error occurred while try to send callback [{}] of task #{}", task.callbackUrl, task.id, it)
            })
    }

    private fun getDownloadUrl(name: String): String =
        UriComponentsBuilder.fromHttpUrl(config.fileBaseUrl)
            .path(name)
            .build()
            .toUriString()

    @KafkaListener(topics = ["encoding-tasks"])
    fun onNewTask(@Payload data: ByteArray, acknowledgment: Acknowledgment) {
        val task = om.readValue(data, Task::class.java)

        sendTaskStatus(task.id, Status.RUNNING)
        val result = pipelineFactory.createPipeline(task).run()
        acknowledgment.acknowledge()
        sendTaskCallback(result)
        sendTaskStatus(task.id, Status.DONE)

    }
}