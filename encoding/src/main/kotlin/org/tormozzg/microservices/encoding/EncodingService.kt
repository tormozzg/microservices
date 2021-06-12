package org.tormozzg.microservices.encoding

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Service
import org.tormozzg.microservices.encoding.api.Task
import org.tormozzg.microservices.encoding.jobs.PipelineFactory
import reactor.core.publisher.Mono

@Service
class EncodingService(
    private val om: ObjectMapper,
    private val pipelineFactory: PipelineFactory
) {

    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    @KafkaListener(topics = ["encoding-tasks"])
    fun onNewTask(@Payload data: ByteArray, acknowledgment: Acknowledgment) {
        Mono.fromCallable {
            om.readValue(data, Task::class.java)
        }
            .flatMap { task ->
                pipelineFactory.createPipeline(task)
                    .run()
            }
            .subscribe({ artifacts ->
                log.info("Task #{} successfully done", artifacts.task.id)
                acknowledgment.acknowledge()
            }, {
                log.error("Error occurred during executing task", it)
            })
    }
}