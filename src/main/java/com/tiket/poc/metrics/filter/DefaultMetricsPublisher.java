package com.tiket.poc.metrics.filter;

import static org.springframework.messaging.support.MessageBuilder.withPayload;
import static reactor.core.publisher.Mono.fromCompletionStage;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.Assert;
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
    Assert.notNull(kafkaTemplate, "Kafka template must be provided");
    this.kafkaTemplate = kafkaTemplate;
  }

  @Override
  public Mono<Void> publish(SampledMetrics sample) {
    return fromCompletionStage(kafkaTemplate.send(withPayload(sample).build()).completable())
        .then();
  }
}
