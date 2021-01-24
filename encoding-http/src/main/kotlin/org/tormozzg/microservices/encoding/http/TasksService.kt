package org.tormozzg.microservices.encoding.http

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.util.concurrent.ListenableFutureCallback
import org.tormozzg.microservices.encoding.api.Status
import org.tormozzg.microservices.encoding.api.Task
import org.tormozzg.microservices.encoding.api.TaskStatus
import reactor.core.publisher.Mono
import reactor.core.publisher.MonoSink
import reactor.kotlin.core.publisher.toMono
import kotlin.math.sin

@Service
class TasksService(
  private val kafkaTemplate: KafkaTemplate<String, ByteArray>,
  private val serviceConfig: EncodingServiceConfig,
  private val objectMapper: ObjectMapper
) {

  private val log: Logger = LoggerFactory.getLogger(this::class.java)

  fun createTask(task: Task): Mono<TaskStatus> = Mono.create { sink ->
    kafkaTemplate.send(serviceConfig.topic, task.id, objectMapper.writeValueAsBytes(task))
      .addCallback(SendCallback(task, sink))
  }

  inner class SendCallback(val task: Task, private val sink: MonoSink<TaskStatus>) :
    ListenableFutureCallback<SendResult<String, ByteArray>?> {
    override fun onSuccess(result: SendResult<String, ByteArray>?) {
      sink.success(TaskStatus(task.id, Status.IN_QUEUE))
    }

    override fun onFailure(ex: Throwable) {
      log.error("Could not send task #{}", task.id, ex)
      sink.error(ex)
    }
  }


}

@ConfigurationProperties(prefix = "encoding")
@ConstructorBinding
data class EncodingServiceConfig(
  val topic: String
)