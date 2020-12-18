package com.tiket.poc.metrics.filter;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.support.MessageBuilder;
import reactor.core.publisher.Mono;

/**
 * Default implementation of {@link MetricsPublisher} which simply publish sampled event into
 * kafka broker.
 *
 * @author zakyalvan
 */
public class DefaultMetricsPublisher implements MetricsPublisher {
  private final KafkaTemplate<Object, Object> kafkaTemplate;

  public DefaultMetricsPublisher(KafkaTemplate<Object, Object> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  @Override
  public Mono<Void> publish(SampledMetrics sample) {
    return Mono.fromCompletionStage(kafkaTemplate.send(MessageBuilder.withPayload(sample).build()).completable())
        .then();
  }
}
