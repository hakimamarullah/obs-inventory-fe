package com.sg.obs.base.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;

@Slf4j
public class WebClientLoggingFilter {

    public static ExchangeFilterFunction logAndHandleErrors(ObjectMapper objectMapper) {
        return ExchangeFilterFunction.ofResponseProcessor(response -> {
            if (response.statusCode().is2xxSuccessful()) {
                return logSuccess(response);
            } else {
                return response.bodyToMono(String.class)
                        .defaultIfEmpty("No body")
                        .flatMap(body -> {
                            try {
                                ApiErrorResponse error = objectMapper.readValue(body, ApiErrorResponse.class);
                                log.error("❌ Error {}: {}", error.code, error.message);
                                return Mono.error(new RuntimeException(error.message));
                            } catch (Exception e) {
                                log.error("❌ Failed to parse error response body: {}", body, e);
                                return Mono.error(new RuntimeException("Unexpected error occurred"));
                            }
                        });
            }
        });
    }

    private static Mono<ClientResponse> logSuccess(ClientResponse response) {
        return response.bodyToMono(String.class)
                .defaultIfEmpty("No body")
                .flatMap(body -> {
                    log.info("✅ Success Response: HTTP {} - {}", response.statusCode(), body);
                    return Mono.just(ClientResponse
                            .create(response.statusCode())
                            .headers(h -> h.addAll(response.headers().asHttpHeaders()))
                            .body(body)
                            .build());
                });
    }

    record ApiErrorResponse(String code, String message) {}
}
