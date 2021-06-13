package org.tormozzg.microservices.encoding.jobs

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class Pipeline constructor(
    private val init: Artifacts,
    private val jobs: Array<Job>
) {

    fun run(): Artifacts {

        var artifacts = init
        return jobs
            .map { job ->
                job.execute(artifacts).also { artifacts = it }
            }
            .last()
    }
}