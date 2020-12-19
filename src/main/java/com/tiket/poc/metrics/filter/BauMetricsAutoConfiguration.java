package com.tiket.poc.metrics.filter;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@link org.springframework.boot.autoconfigure.EnableAutoConfiguration} for publishing train bau metrics.
 *
 * @author zakyalvan
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(BauMetricsProperties.class)
@ConditionalOnWebApplication(type = Type.REACTIVE)
@ConditionalOnProperty(prefix = BauMetricsProperties.CONFIGURATION_PREFIX,
    name = BauMetricsProperties.ENABLED_PROPERTIES, matchIfMissing = true)
class BauMetricsAutoConfiguration {
  @Autowired
  private BauMetricsProperties metricsProperties;

  @Bean
  MetricsPublishingFilter metricsPublishingFilter(ObjectProvider<MetricsPublisher> publishers) {
    MetricsPublisher publisher = publishers.getIfUnique(() -> MetricsPublisher.LOGGING_ONLY);
    return new MetricsPublishingFilter(publisher);
  }
}
