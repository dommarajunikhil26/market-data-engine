package com.nikhil.marketdataengine.service;

import com.nikhil.marketdataengine.repository.PriceTickRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.lang.invoke.MethodHandles;

@Service
public class PersistenceService {
    private final PriceTickRepository priceTickRepository;
    private final BinanceWebSocketService binanceWebSocketService;
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public PersistenceService(PriceTickRepository priceTickRepository, BinanceWebSocketService binanceWebSocketService) {
        this.priceTickRepository = priceTickRepository;
        this.binanceWebSocketService = binanceWebSocketService;
    }

    @PostConstruct
    public void startPersistence() {
        binanceWebSocketService.getPriceStream()
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(tick -> logger.info("About to save: {} @ {}", tick.getSymbol(), tick.getPrice()))
                .flatMap(tick -> priceTickRepository.save(tick)
                        .doOnError(e -> logger.error("Failed to save tick: {}", e.getMessage()))
                        .onErrorResume(e -> Mono.empty())
                )
                .doOnNext(price -> logger.info("Saving id: {}", price.getId()))
                .subscribe(
                        saved -> logger.debug("Saved tick: {}", saved.getSymbol()),
                        error -> logger.error("Persistence error", error)
                );
    }
}
