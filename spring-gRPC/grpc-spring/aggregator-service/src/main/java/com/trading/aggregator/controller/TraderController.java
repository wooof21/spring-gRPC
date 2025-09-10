package com.trading.aggregator.controller;

import com.trading.aggregator.service.TraderService;
import com.trading.trader.TraderInformation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/trader")
public class TraderController {

    @Autowired
    private TraderService traderService;

    @GetMapping(value = "{traderId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public TraderInformation getTraderInformation(@PathVariable Integer traderId) {
        return this.traderService.getTraderInformation(traderId);
    }
}
