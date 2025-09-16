package com.trading.aggregator.v2.controller;

import com.trading.aggregator.v2.service.TraderService;
import com.trading.aggregator.v2.utils.ProtoJsonUtil;
import com.trading.trader.v2.TraderInformation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v2/trader")
@Slf4j
public class TraderController {

    @Autowired
    private TraderService traderService;

    /**
     * Spring WebFlux `ServerCodecConfigurer` only works with `HttpMessageReader/HttpMessageWriter`,
     * not Spring MVC `HttpMessageConverters`, and `ProtobufJsonFormatHttpMessageConverter` is a
     * MVC `HttpMessageConverter`
     *
     * Need manually to convert proto Message to JSON
     */
    @GetMapping(value = "{traderId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> getTraderInformation(@PathVariable Integer traderId) {
        return Mono.fromSupplier(() ->
                ProtoJsonUtil.toJson(traderService.getTraderInformation(traderId)));
    }
}
