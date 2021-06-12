package org.tormozzg.microservices.encoding.jobs

import org.springframework.stereotype.Component
import org.tormozzg.microservices.encoding.api.Task
import org.tormozzg.microservices.encoding.api.TaskType

@Component
class PipelineFactory(
    private val downloadJob: DownloadJob,
    private val encodeJob: EncodeJob
) {

    fun createPipeline(task: Task): Pipeline =
        when (task.type) {
            TaskType.ENCODE -> {
                Pipeline(Artifacts(task, null), arrayOf(downloadJob, encodeJob))
            }
            TaskType.PREVIEW -> {
                TODO()
            }
        }
}