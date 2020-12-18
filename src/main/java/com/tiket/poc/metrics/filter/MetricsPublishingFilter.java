package com.tiket.poc.metrics.filter;

import com.tiket.poc.metrics.filter.MetricsPublisher.EndpointLatency;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * @author zakyalvan
 */
@Slf4j
public class MetricsPublishingFilter implements WebFilter {
  private final MetricsPublisher metricsPublisher;

  public MetricsPublishingFilter(MetricsPublisher metricsPublisher) {
    this.metricsPublisher = metricsPublisher;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    String endpointPath = exchange.getRequest().getPath().value();
    HttpMethod requestMethod = exchange.getRequest().getMethod();

    return EndpointLatency.start(endpointPath, requestMethod)
        .flatMap(recorder -> chain.filter(exchange)
            .doOnSubscribe(subscription -> exchange.getResponse()
                .beforeCommit(() -> metricsPublisher
                    .publish(recorder.complete(exchange.getResponse().getStatusCode(), "SUCCESS")))
            )
        )
        .subscriberContext(context -> context.put(DomainEnricher.class, DomainEnricher.empty()))
        .subscribeOn(Schedulers.parallel());
  }
}
