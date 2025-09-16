package com.trading.aggregator.v2.controller;

import com.trading.aggregator.v2.model.PriceUpdateModel;
import com.trading.aggregator.v2.service.StockPriceUpdateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/v2/stock")
@Slf4j
public class StockController {


    @Autowired
    private StockPriceUpdateService service;

    @GetMapping(value = "updates", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<PriceUpdateModel> streamPrices() {
        return service.getPriceUpdates();
    }
}
