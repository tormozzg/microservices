package org.tormozzg.microservices.encoding.jobs

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class Pipeline constructor(
    private val init: Artifacts,
    private val jobs: Array<Job>
) {

    fun run(): Mono<Artifacts> {
        return Flux.fromArray(jobs)
            .flatMap { job ->
                Mono.deferWithContext { ctx ->
                    val artifacts = ctx.getOrDefault("artifacts", init)!!
                    Mono.fromCallable {
                        job.execute(artifacts)
                            .also { ctx.put("artifacts", it) }
                    }
                }

            }
            .last()
    }

}