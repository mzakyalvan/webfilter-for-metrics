package com.tiket.poc.metrics.filter;

import java.util.HashMap;
import java.util.Map;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.http.HttpMethod;
import org.springframework.validation.annotation.Validated;

/**
 * @author zakyalvan
 */
@Data
@Validated
@ConfigurationProperties(BauMetricsProperties.CONFIGURATION_PREFIX)
class BauMetricsProperties {
  public static final String CONFIGURATION_PREFIX = "tiket.train.bau.metrics";
  public static final String ENABLED_PROPERTIES = "enabled";

  private boolean publishEnabled;

  @Valid
  @NestedConfigurationProperty
  private final Map<String, @Valid @NotNull EndpointMapping> endpoints = new HashMap<>();

  @Valid
  @NestedConfigurationProperty
  private final PublisherSettings publisher = new PublisherSettings();

  @Data
  static class EndpointMapping {
    /**
     * Endpoint path.
     */
    @NotBlank
    private String path;

    /**
     * Regex.
     */
    private boolean regex;

    /**
     * Http method used on endpoint.
     */
    @NonNull
    private HttpMethod method;

    /**
     * Business name for given endpoint.
     */
    @NotBlank
    private String name;
  }

  @Data
  static class PublisherSettings {
    @Valid
    private final KafkaPublisher kafka = new KafkaPublisher();
  }

  @Data
  static class KafkaPublisher {

  }
}
