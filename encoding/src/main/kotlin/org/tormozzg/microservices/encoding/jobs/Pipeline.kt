package org.tormozzg.microservices.encoding.jobs

import org.tormozzg.microservices.encoding.api.Task
import java.io.File

class Pipeline constructor(
    private val task: Task,
    private val jobs: Array<Job>
) {

    fun run(): PipelineResult {
        var artifacts = Artifacts(task, null)
        return try {
            val resultArtifact = jobs
                .map { job ->
                    job.execute(artifacts).also { artifacts = it }
                }
                .last()
            PipelineResult(task, resultArtifact.resultFile!!, null)
        } catch (e: Exception) {
            PipelineResult(task, null, e)
        }
    }
}

class PipelineResult(
    val task: Task,
    val resultFile: File?,
    val error: Throwable?
)