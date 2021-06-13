package org.tormozzg.microservices.encoding.jobs

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class Pipeline constructor(
    private val init: Artifacts,
    private val jobs: Array<Job>
) {

    fun run(): Mono<Artifacts> {
        return Flux.defer {
            var artifacts = init
            Flux.fromArray(jobs)
                .concatMap { job ->
                    Mono.fromCallable {
                        job.execute(artifacts)
                            .also { artifacts = it }
                    }
                }
        }
            .last()
    }
}