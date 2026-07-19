package com.nikhil.marketdataengine.service;

import com.nikhil.marketdataengine.model.AlertRule;
import com.nikhil.marketdataengine.model.PriceTick;
import com.nikhil.marketdataengine.model.TriggeredAlert;
import com.nikhil.marketdataengine.repository.AlertRuleRepository;
import com.nikhil.marketdataengine.repository.TriggeredAlertRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.lang.invoke.MethodHandles;

@Service
public class AlertEngine {
    private static final Logger logger =  LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final AlertRuleRepository  alertRuleRepository;
    private final TriggeredAlertRepository triggeredAlertRepository;
    private final BinanceWebSocketService binanceWebSocketService;

    public AlertEngine(AlertRuleRepository alertRuleRepository, BinanceWebSocketService binanceWebSocketService,
                       TriggeredAlertRepository triggeredAlertRepository) {
        this.alertRuleRepository = alertRuleRepository;
        this.binanceWebSocketService = binanceWebSocketService;
        this.triggeredAlertRepository = triggeredAlertRepository;
    }

    private final Sinks.Many<TriggeredAlert> alertSink =
            Sinks.many().multicast().onBackpressureBuffer();

    public Flux<TriggeredAlert> getAlertStream() {
        return alertSink.asFlux();
    }

    @PostConstruct
    public void startPersistence(){
        binanceWebSocketService.getPriceStream()
                .groupBy(PriceTick::getSymbol)
                .flatMap(groupedFlux -> {
                    String symbol = groupedFlux.key();

                    return groupedFlux.flatMap(tick ->
                            alertRuleRepository.findBySymbolAndActiveTrue(symbol)
                                    .filter(alertRule -> isTriggered(alertRule, tick.getPrice()))
                                    .flatMap(rule-> {
                                        TriggeredAlert alert = TriggeredAlert.builder()
                                                .alertRuleId(rule.getId())
                                                .symbol(symbol)
                                                .price(tick.getPrice())
                                                .build();
                                        rule.setActive(false);
                                        return triggeredAlertRepository.save(alert)
                                                .flatMap(savedAlert -> alertRuleRepository.save(rule)
                                                        .thenReturn(savedAlert));
                                    })
                                    .doOnNext(alertSink::tryEmitNext)
                    );
                })
                .subscribe(
                        saved -> logger.info("Saved alert for {}:", saved.getSymbol()),
                        error -> logger.error("Error saving alert: ", error)
                );
    }

    public boolean isTriggered(AlertRule rule, double price) {
        return switch (rule.getAlertCondition()) {
            case ABOVE -> price > rule.getThreshold();
            case BELOW -> price < rule.getThreshold();
        };
    }
}
