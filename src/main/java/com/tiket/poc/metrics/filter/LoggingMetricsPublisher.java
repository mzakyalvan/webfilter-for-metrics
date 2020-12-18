package com.tiket.poc.metrics.filter;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * @author zakyalvan
 */
@Slf4j
class LoggingMetricsPublisher implements MetricsPublisher {
  @Override
  public Mono<Void> publish(SampledMetrics metrics) {
    return Mono.<Void>empty()
        .doOnSubscribe(s -> log.info("Publishing metrics {}", metrics));
  }
}
