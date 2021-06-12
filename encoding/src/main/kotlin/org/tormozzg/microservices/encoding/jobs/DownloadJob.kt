package org.tormozzg.microservices.encoding.jobs

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.tormozzg.microservices.encoding.EncodingConfiguration
import org.tormozzg.microservices.encoding.api.Task
import org.tormozzg.microservices.encoding.logTags
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.nio.channels.Channels
import javax.annotation.PostConstruct

@Component
class DownloadJob(
    private val config: EncodingConfiguration
) : AbstractJob() {

    private val tmpFileDir: File = File(config.tmpDirectory)

    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    @PostConstruct
    fun init() {
        if (tmpFileDir.exists()) {
            if (tmpFileDir.isFile)
                throw IllegalStateException("Invalid path to temporary directory. It is a file")

            if (!tmpFileDir.canWrite())
                throw IllegalStateException("Directory of temporary file is not writable")
        } else {
            if (!tmpFileDir.mkdirs())
                throw IllegalStateException("Could not create directory of temporary files")
        }
    }

    override fun doJob(artifacts: Artifacts): File {
        return logTags(
            "TaskId" to artifacts.task.id,
            "Job" to name
        ) {
            val task = artifacts.task
            log.debug("Start download job for task {}", task.id)
            return@logTags (URL(task.fileUrl).openConnection() as HttpURLConnection)
                .apply {
                    requestMethod = "GET"
                    connectTimeout = config.connectTimeout.toMillis().toInt()
                    readTimeout = config.readTimeout.toMillis().toInt()
                    connect()
                }
                .let { con ->
                    val status = con.responseCode
                    if (status == 200) {
                        val dist = File(tmpFileDir, getFileName(task))
                        val downloadSize = Channels.newChannel(con.inputStream).use { rc ->
                            FileOutputStream(dist).use { fos ->
                                fos.channel.use {
                                    it.transferFrom(rc, 0, Long.MAX_VALUE)
                                }
                            }
                        }
                        log.debug(
                            "File {} has been successfully downloaded. Number of downloaded bytes is {}",
                            task.fileUrl,
                            downloadSize
                        )

                        dist
                    } else {
                        log.error("Could not download file. Illegal response status {}", status)
                        throw IOException("Could not download file. Illegal status $status")
                    }
                }
        }
    }

    private fun getFileName(task: Task): String = "${task.id}.download"

    override val name: String = "Download"
}