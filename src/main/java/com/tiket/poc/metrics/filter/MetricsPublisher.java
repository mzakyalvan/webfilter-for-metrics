package com.tiket.poc.metrics.filter;

import java.time.Duration;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;

/**
 * Type responsible in publishing metrics to aggregator.
 *
 * @author zakyalvan
 */
public interface MetricsPublisher {
  MetricsPublisher LOGGING_ONLY = new LoggingMetricsPublisher();

  Mono<Void> publish(SampledMetrics sample);

  /**
   * Contract for metrics.
   */
  interface SampledMetrics {
  }

  @Value
  @Getter
  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  class EndpointLatency implements SampledMetrics {
    String endpointPath;
    HttpMethod requestMethod;
    LocalDateTime startTime;
    Duration execDuration;
    HttpStatus responseCode;
    String businessCode;

    public static Mono<Recorder> start(String endpointPath, HttpMethod httpMethod) {
      return Mono.fromCallable(() -> new Recorder(endpointPath, httpMethod));
    }

    public static class Recorder {
      private final String endpointPath;
      private final HttpMethod httpMethod;
      private final LocalDateTime startTime;
      private final long elapseStart;

      private Recorder(String endpointPath, HttpMethod httpMethod) {
        this.endpointPath = endpointPath;
        this.httpMethod = httpMethod;

        this.startTime = LocalDateTime.now();
        this.elapseStart = System.nanoTime();
      }

      public SampledMetrics complete(HttpStatus responseCode, String businessCode) {
        return new EndpointLatency(endpointPath, httpMethod, startTime, Duration.ofNanos(System.nanoTime() - elapseStart), responseCode, businessCode);
      }
    }
  }
}
