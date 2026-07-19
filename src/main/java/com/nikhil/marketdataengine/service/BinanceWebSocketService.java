package com.nikhil.marketdataengine.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nikhil.marketdataengine.dto.BinanceTickerMessage;
import com.nikhil.marketdataengine.model.PriceTick;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;

@Service
public class BinanceWebSocketService {
    private static final String BINANCE_WS_URL = "wss://stream.binance.us:9443/stream?streams=btcusdt@ticker/ethusdt@ticker/solusdt@ticker";
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final WebSocketClient webSocketClient = new ReactorNettyWebSocketClient();
    private final ObjectMapper objectMapper;
    private final Sinks.Many<PriceTick> sink = Sinks.many().multicast().onBackpressureBuffer();

    public BinanceWebSocketService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void connect() {
        startConnection()
                .retryWhen(Retry.backoff(5, Duration.ofSeconds(1))
                        .maxBackoff(Duration.ofSeconds(30))
                        .doBeforeRetry(retrySignal ->  {
                            logger.warn("WebSocket connection dropped. Retrying attempt #{} due to: {}", retrySignal.totalRetries()+1, retrySignal.failure().getMessage());
                        })
                ).subscribe(
                        null,
                error -> logger.error("WebSocket permanently failed after maximum retries", error),
                        () -> logger.info("WebSocket connection closed cleanly")
                );

    }

    private Mono<Void> startConnection() {
        return webSocketClient.execute(
                URI.create(BINANCE_WS_URL),
                session -> session.receive()
                        .map(WebSocketMessage::getPayloadAsText)
                        .flatMap(this::parseMessage)
                        .doOnNext(tick -> logger.info("Received tick: {} @ {}", tick.getSymbol(), tick.getPrice()))
                        .doOnNext(tick -> {
                            Sinks.EmitResult result = sink.tryEmitNext(tick);
                            if (result.isFailure()) {
                                logger.warn("Failed to emit tick for {}: {}", tick.getSymbol(), result);
                            }
                        })
                        .then()
        );
    }

    private Mono<PriceTick> parseMessage(String json) {
        return Mono.fromCallable(() -> {
            BinanceTickerMessage message = objectMapper.readValue(json, BinanceTickerMessage.class);
            return mapToPriceTick(message);
        })
                .subscribeOn(Schedulers.boundedElastic())
                .onErrorResume(e -> {
                    logger.error("Error while parsing message", e);
                    return Mono.empty();
                });
    }

    private PriceTick mapToPriceTick(BinanceTickerMessage message) {
        return PriceTick.builder()
                .symbol(message.data().symbol())
                .price(Double.parseDouble(message.data().price()))
                .timestamp(Instant.ofEpochMilli(message.data().eventTime()))
                .build();
    }

    public Flux<PriceTick> getPriceStream() {
        return sink.asFlux();
    }
}

