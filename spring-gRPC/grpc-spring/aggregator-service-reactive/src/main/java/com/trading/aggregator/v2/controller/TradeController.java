package com.trading.aggregator.v2.controller;

import com.trading.aggregator.v2.service.TradeService;
import com.trading.aggregator.v2.utils.ProtoJsonUtil;
import com.trading.trader.v2.StockTradeRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v2/trade")
@Slf4j
public class TradeController {

    @Autowired
    private TradeService tradeService;

    /**
     * When want to send the RequestBody as JSON string,
     * need to convert the JSON string to ProtoBuf binary format,
     * so that gRPC is able to parse the request
     *
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> trade(@RequestBody String json) {
        StockTradeRequest request = ProtoJsonUtil.fromJson(
                json,
                StockTradeRequest::newBuilder
        );
        return Mono.fromSupplier(() -> ProtoJsonUtil.toJson(tradeService.trade(request)));
    }
}
