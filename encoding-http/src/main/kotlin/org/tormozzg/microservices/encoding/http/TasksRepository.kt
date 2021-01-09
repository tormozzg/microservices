package org.tormozzg.microservices.encoding.http

import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.cloud.client.ServiceInstance
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.cloud.netflix.hystrix.ReactiveHystrixCircuitBreakerFactory
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriComponentsBuilder
import org.tormozzg.microservices.encoding.api.Status
import org.tormozzg.microservices.encoding.api.TaskStatus
import org.tormozzg.microservices.encoding.api.http.ListTasksStatuses
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import reactor.netty.tcp.TcpClient
import java.util.concurrent.TimeUnit

interface TasksRepository {
  fun fetchList(status: Status? = null, amount: Int, offset: Int): Mono<ListTasksStatuses>
}


@Component
class TasksRepositoryImp(
    private val discoveryClient: DiscoveryClient,
    private val cbFactory: ReactiveHystrixCircuitBreakerFactory
) : TasksRepository {

  private val cb = cbFactory.create("encoding")

  private val log: Logger = LoggerFactory.getLogger(this::class.java)

  private val webClient: WebClient = WebClient.builder()
      .clientConnector(ReactorClientHttpConnector(HttpClient.from(
          TcpClient.create()
              .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
              .doOnConnected {
                it.addHandlerLast(ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS))
                it.addHandlerLast(WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS))
              }
      )))
      .build()

  override fun fetchList(status: Status?, amount: Int, offset: Int): Mono<ListTasksStatuses> {

    val instances = discoveryClient.getInstances("encoding")
    var totalFetched = 0
    var nextOffset = offset

    return Flux.fromIterable(instances)
        .flatMapSequential {
          val am = if (totalFetched < amount) amount - totalFetched else 0
          callFetch(it, status, am, nextOffset)
        }
        .doOnNext {
          if (offset > 0) {
            nextOffset -= it.count
          }
          totalFetched += it.items.size
        }
        .collectList()
        .map { instanceList ->
          var totalCount = 0
          val list = mutableListOf<TaskStatus>()
          instanceList.forEach {
            totalCount += it.count
            list.addAll(it.items)
          }

          ListTasksStatuses(totalCount, list)
        }

  }


  fun callFetch(instance: ServiceInstance, status: Status?, amount: Int, offset: Int): Mono<ListTasksStatuses> {
    val instanceUri = instance.uri

    return Mono.defer {
      log.info("Requesting instance {}", instanceUri)
      val urlBuilder = UriComponentsBuilder.fromUri(instanceUri)
          .path("/tasks")
          .queryParam("amount", amount)
          .queryParam("offset", offset)
      if (status != null)
        urlBuilder.queryParam("status", status.name)

      webClient.get()
          .uri(urlBuilder.toUriString())
          .retrieve()
          .bodyToMono(ListTasksStatuses::class.java)
          .transform {
            cb.run(it) { e: Throwable ->
              log.error("CB: Could not fetch list of tasks of instance {}", instanceUri, e)
              Mono.just(ListTasksStatuses(0, emptyList()))
            }
          }

    }
        .onErrorResume { e ->
          log.error("Could not fetch list of tasks of instance {}", instanceUri, e)
          Mono.just(ListTasksStatuses(0, emptyList()))
        }
  }


}