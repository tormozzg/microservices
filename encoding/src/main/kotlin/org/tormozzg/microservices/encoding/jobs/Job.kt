package org.tormozzg.microservices.encoding.jobs

import org.tormozzg.microservices.encoding.api.Task
import java.io.File


data class Artifacts(
    val task: Task,
    val resultFile: File?
)

interface Job {
    fun execute(artifacts: Artifacts): Artifacts
    val name: String
}

abstract class AbstractJob : Job {
    override fun execute(artifacts: Artifacts): Artifacts {
        val result = try {
            doJob(artifacts)
        } catch (e: Exception) {
            throw e
        }
        val task = artifacts.task
        return Artifacts(task, result)
    }

    abstract fun doJob(artifacts: Artifacts): File
}

