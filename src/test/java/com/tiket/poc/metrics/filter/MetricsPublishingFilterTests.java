package com.tiket.poc.metrics.filter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.tiket.poc.metrics.filter.TestingConfiguration.Order;
import com.tiket.poc.metrics.filter.TestingConfiguration.PaymentProcessor;
import com.tiket.poc.metrics.filter.TestingConfiguration.ServiceResponse;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

/**
 * @author zakyalvan
 */
@SpringBootTest(classes = TestingConfiguration.class, webEnvironment = RANDOM_PORT, properties = {
    "server.netty.connection-timeout=1s"
})
class MetricsPublishingFilterTests {
  @Autowired
  private WebTestClient testClient;

  @MockBean
  private PaymentProcessor paymentProcessor;

  @Test
  void metricsEnricherShouldBeAvailableDownStream() {
    String orderIdentifier = "1234567";
    String orderSecret = UUID.randomUUID().toString();

    when(paymentProcessor.process(eq(orderIdentifier), eq(orderSecret)))
        .thenReturn(Mono.just(Order.builder()
            .id(orderIdentifier).customerEmail("zaky.alvan@tiket.com").totalPrice(BigDecimal.TEN)
            .lineItems(Arrays.asList("First Item", "Second Item"))
            .createdTime(LocalDateTime.now().minusMinutes(10)).issuedTime(LocalDateTime.now())
            .orderChannel("apps")
            .build()));

    testClient.post()
        .uri(builder -> builder.path("/orders/{id}/payments")
            .queryParam("order-secret", "{secret}")
            .build(orderIdentifier, orderSecret))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk()
        .expectBody(ServiceResponse.class)
        .value(response -> {
          assertThat(response.getCode()).isEqualTo("SUCCESS");
          assertThat(response.getMessage()).isEqualTo("Success");
        });
  }

  @Test
  void whenClientRequestTimedOut_thenShouldReportFailure() {
    String orderIdentifier = "1234567";
    String orderSecret = UUID.randomUUID().toString();

    when(paymentProcessor.process(eq(orderIdentifier), eq(orderSecret)))
        .thenReturn(Mono.<Order>empty().delaySubscription(Duration.ofSeconds(6)));

    testClient.post()
        .uri(builder -> builder.path("/orders/{id}/payments")
            .queryParam("order-secret", "{secret}")
            .build(orderIdentifier, orderSecret))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk()
        .expectBody(ServiceResponse.class)
        .value(response -> {

        });
  }

  @Test
  void whenUnknownServerErrorThrown_thenShouldReportFailure() {
    String orderIdentifier = "1234567";
    String orderSecret = UUID.randomUUID().toString();

    when(paymentProcessor.process(eq(orderIdentifier), eq(orderSecret)))
        .thenReturn(Mono.error(new IllegalStateException("Boom!")));

    testClient.post()
        .uri(builder -> builder.path("/orders/{id}/payments")
            .queryParam("order-secret", "{secret}")
            .build(orderIdentifier, orderSecret))
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk()
        .expectBody(ServiceResponse.class)
        .value(response -> {

        });
  }
}