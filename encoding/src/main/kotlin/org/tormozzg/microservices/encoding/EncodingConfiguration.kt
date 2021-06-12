package org.tormozzg.microservices.encoding

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import java.time.Duration

@ConfigurationProperties(prefix = "encoding")
@ConstructorBinding
data class EncodingConfiguration(
    val tmpDirectory: String = "cache",
    val completeDirectory: String = "complete",
    val ffmpeg: String = "ffmpeg",
    val ffprobe: String = "ffprobe",
    val workerThreads: Int = Runtime.getRuntime().availableProcessors(),
    val tmpFileKeepAlive: Duration = Duration.ofMinutes(15),
    val completeFileKeepAlive: Duration = Duration.ofMinutes(20),
    val cleanCron: String = "0 0 */15 * * *",
    val connectTimeout: Duration = Duration.ofSeconds(3),
    val readTimeout: Duration = Duration.ofSeconds(3),
    val writeTimeout: Duration = Duration.ofSeconds(3)
)