package com.nikhil.marketdataengine.controller;

import com.nikhil.marketdataengine.model.PriceTick;
import com.nikhil.marketdataengine.model.TriggeredAlert;
import com.nikhil.marketdataengine.service.AlertEngine;
import com.nikhil.marketdataengine.service.BinanceWebSocketService;
import com.nikhil.marketdataengine.utils.AverageTracker;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/streams")
public class MarketDataController {

    private final BinanceWebSocketService binanceWebSocketService;
    private final AlertEngine alertEngine;

    public MarketDataController(BinanceWebSocketService binanceWebSocketService, AlertEngine alertEngine) {
        this.binanceWebSocketService = binanceWebSocketService;
        this.alertEngine = alertEngine;
    }

    @GetMapping(value = "/prices", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<PriceTick> streamPrices(){
        return binanceWebSocketService.getPriceStream();
    }

    @GetMapping(value = "/alerts", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<TriggeredAlert> streamAlertRules(){
        return alertEngine.getAlertStream();
    }

    @GetMapping(value = "/averages/{symbol}",  produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Double> streamAverages(@PathVariable String symbol) {
        return binanceWebSocketService.getPriceStream()
                .filter(priceTick -> priceTick.getSymbol().equals(symbol))
                .scan(new AverageTracker(0.0, 0.0), (tracker, price) -> tracker.next(price.getPrice()))
                .skip(1)
                .map(AverageTracker::getAverage);
    }
}
