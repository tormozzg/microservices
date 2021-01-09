package org.tormozzg.microservices.encoding

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

@Service
class EncodingService {

  private val log: Logger = LoggerFactory.getLogger(this::class.java)

  @KafkaListener(topics = ["encoding-tasks"])
  fun onNewTask(record: ConsumerRecord<String, ByteArray>) {
    log.info("New message. Key: {}. Data: {}", record.key(), String(record.value()))
  }
}