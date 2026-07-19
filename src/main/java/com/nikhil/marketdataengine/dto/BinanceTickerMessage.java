package com.nikhil.marketdataengine.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BinanceTickerMessage(
        String stream,
        BinanceTickerData data
) {
    public record BinanceTickerData(
            @JsonProperty("s") String symbol,
            @JsonProperty("c") String price,
            @JsonProperty("E") long eventTime
    ) {}
}
