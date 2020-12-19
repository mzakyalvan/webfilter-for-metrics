package com.tiket.poc.metrics.filter;

import static com.tiket.poc.metrics.filter.BusinessAttributes.empty;
import static reactor.core.publisher.Mono.subscriberContext;

import com.tiket.poc.metrics.filter.MetricsPublisher.EndpointLatency;
import com.tiket.poc.metrics.filter.MetricsPublisher.SampledMetrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * {@link WebFilter} which responsible to publish metrics for endpoint.
 *
 * @author zakyalvan
 */
@Slf4j
public class MetricsPublishingFilter implements WebFilter {
  private final MetricsPublisher metricsPublisher;
  private boolean publishEnabled;

  public MetricsPublishingFilter(MetricsPublisher metricsPublisher) {
    this.metricsPublisher = metricsPublisher;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    if(!publishEnabled) {
      return chain.filter(exchange);
    }

    String endpointPath = exchange.getRequest().getPath().value();
    HttpMethod requestMethod = exchange.getRequest().getMethod();

    return subscriberContext()
        .flatMap(context -> EndpointLatency.start(endpointPath, requestMethod)
            .flatMap(recorder -> chain.filter(exchange)
                .doOnSubscribe(subscription -> exchange.getResponse()
                    .beforeCommit(() -> {
                      BusinessAttributes attributes = context.get(BusinessAttributes.class);
                      SampledMetrics latencyMetrics = recorder
                          .complete(exchange.getResponse().getStatusCode(), (String) attributes.get("businessCode"));
                      return metricsPublisher.publish(latencyMetrics);
                    })
                )
            )
        )
        .subscriberContext(context -> context.put(BusinessAttributes.class, empty()))
        .subscribeOn(Schedulers.parallel());
  }
}
