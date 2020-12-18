package com.tiket.poc.metrics.filter;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zakyalvan
 */
@Configuration
public class BauMetricsAutoConfiguration {
  @Bean
  MetricsPublishingFilter metricsPublishingFilter(ObjectProvider<MetricsPublisher> publishers) {
    MetricsPublisher publisher = publishers.getIfUnique(() -> MetricsPublisher.LOGGING_ONLY);
    return new MetricsPublishingFilter(publisher);
  }
}
