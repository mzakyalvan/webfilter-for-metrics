package com.tiket.poc.metrics.filter;

import static com.tiket.poc.metrics.filter.TestingConfiguration.ServiceResponse.succeed;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.queryParam;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;
import static reactor.core.publisher.Mono.defer;
import static reactor.core.publisher.Mono.subscriberContext;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.NestedRuntimeException;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/**
 * @author zakyalvan
 */
@Slf4j
@EnableAutoConfiguration
@Configuration(proxyBeanMethods = false)
public class TestingConfiguration {
  @Bean
  RouterFunction<ServerResponse> processPaymentEndpoint(ObjectProvider<PaymentProcessor> processors) {
    RequestPredicate predicate = POST("/orders/{id}/payments")
        .and(queryParam("order-secret", StringUtils::hasText))
        .and(accept(MediaType.APPLICATION_JSON));

    PaymentProcessor processor = processors.getIfUnique(() ->
        (id, secret) -> Mono.error(new IllegalStateException("No payment processor configured")));

    HandlerFunction<ServerResponse> handler = request -> subscriberContext()
        .flatMap(context -> defer(() -> processor.process(request.pathVariable("id"), request.queryParam("order-secret").get()))
            .doOnNext(order -> {
              Optional<BusinessAttributes> metricsOptional = context.getOrEmpty(BusinessAttributes.class);
              metricsOptional.ifPresent(metrics -> {
                metrics.put("issuanceLag", Duration.between(order.getCreatedTime(), order.getIssuedTime()));
              });
              metricsOptional.ifPresent(metrics -> metrics.put("businessCode", "SUCCESS"));
            })
            .doOnError(throwable -> {
              Optional<BusinessAttributes> metricsOptional = context.getOrEmpty(BusinessAttributes.class);
              metricsOptional.ifPresent(metrics -> metrics.put("businessCode", "ERROR"));
            })
            .flatMap(order -> ok().bodyValue(succeed(order)))
        );

    return RouterFunctions.route(predicate, handler);
  }

  public interface PaymentProcessor {
    Mono<Order> process(String id, String secret);
  }

  @Getter
  public static class ProcessPaymentException extends NestedRuntimeException {
    private final ProblemType problem;

    ProcessPaymentException(ProblemType problem, String message, Throwable cause) {
      super(message, cause);
      this.problem = problem;
    }

    public static <T> Mono<T> orderNotFoundError() {
      return Mono.error(new ProcessPaymentException(ProblemType.ORDER_NOT_FOUND, "Order not found", null));
    }

    public static <T> Mono<T> alreadyCancelledError() {
      return Mono.error(new ProcessPaymentException(ProblemType.ORDER_ALREADY_CANCELLED, "Order already cancelled", null));
    }

    public static <T> Mono<T> alreadyPaidError() {
      return Mono.error(new ProcessPaymentException(ProblemType.ORDER_ALREADY_PAID, "Order already paid", null));
    }

    public enum ProblemType {
      ORDER_NOT_FOUND, ORDER_ALREADY_CANCELLED, ORDER_ALREADY_PAID
    }
  }

  @Value
  @Getter
  public static class Order {
    String id;
    String customerEmail;
    BigDecimal totalPrice;
    List<String> lineItems;
    LocalDateTime createdTime;
    LocalDateTime cancelledTime;
    LocalDateTime issuedTime;
    String orderChannel;

    @Builder
    @JsonCreator
    Order(String id, String customerEmail, BigDecimal totalPrice, List<String> lineItems,
        LocalDateTime createdTime, LocalDateTime cancelledTime, LocalDateTime issuedTime,
        String orderChannel) {
      this.id = id;
      this.customerEmail = customerEmail;
      this.totalPrice = totalPrice;
      this.lineItems = lineItems;
      this.createdTime = createdTime;
      this.cancelledTime = cancelledTime;
      this.issuedTime = issuedTime;
      this.orderChannel = orderChannel;
    }
  }

  @Value
  @Getter
  public static class ServiceResponse<P> {
    String code;
    String message;
    List<String> errors;
    P data;
    Date serverTime;

    @JsonCreator
    ServiceResponse(String code, String message, List<String> errors, P data,
        Date serverTime) {
      this.code = code;
      this.message = message;
      this.errors = errors;
      this.data = data;
      this.serverTime = serverTime;
    }

    public static <P> ServiceResponse<P> succeed(P data) {
      return new ServiceResponse<>("SUCCESS", "Success", Collections.emptyList(), data, new Date());
    }
    public static <P> ServiceResponse<P> failed(String code, String message) {
      return new ServiceResponse<>(code, message, Collections.emptyList(), null, new Date());
    }
    public static <P> ServiceResponse<P> failed(String code, String message, List<String> errors) {
      return new ServiceResponse<>(code, message, errors, null, new Date());
    }
  }
}
