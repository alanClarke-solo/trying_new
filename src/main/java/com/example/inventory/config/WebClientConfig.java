package com.example.inventory.config;

import com.example.inventory.exception.ExternalServiceException;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient() {
        // Configure HttpClient with timeouts
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .responseTimeout(Duration.ofMillis(5000))
                .doOnConnected(conn -> 
                    conn.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS)));

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter(errorHandlingFilter())
                .build();
    }

    /**
     * Filter that handles WebClient errors and transforms them into our custom exceptions
     */
    private ExchangeFilterFunction errorHandlingFilter() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (clientResponse.statusCode().is5xxServerError()) {
                return clientResponse.bodyToMono(String.class)
                        .flatMap(errorBody -> Mono.error(new ExternalServiceException(
                                "External service error: " + errorBody,
                                clientResponse.statusCode().equals(HttpStatus.SERVICE_UNAVAILABLE) ?
                                        HttpStatus.SERVICE_UNAVAILABLE : HttpStatus.BAD_GATEWAY)));
            } else if (clientResponse.statusCode().is4xxClientError()) {
                return clientResponse.bodyToMono(String.class)
                        .flatMap(errorBody -> {
                            HttpStatus status = HttpStatus.resolve(clientResponse.rawStatusCode());
                            if (status == null) {
                                status = HttpStatus.BAD_REQUEST;
                            }
                            return Mono.error(new ExternalServiceException(
                                    "External service client error: " + errorBody, status));
                        });
            }
            return Mono.just(clientResponse);
        });
    }
}
