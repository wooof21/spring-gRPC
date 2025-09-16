package com.trading.aggregator.v2.service;

import com.trading.stock.v2.StockPriceRequest;
import com.trading.stock.v2.StockServiceGrpc;
import com.trading.trader.v2.StockTradeRequest;
import com.trading.trader.v2.StockTradeResponse;
import com.trading.trader.v2.TraderServiceGrpc;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;

@Slf4j
@Service
public class TradeService {

    @GrpcClient("trader-service")
    private TraderServiceGrpc.TraderServiceBlockingStub traderClient;

    @GrpcClient("stock-service")
    private StockServiceGrpc.StockServiceBlockingStub stockClient;

    @Autowired
    private ExecutorService executor;

    public StockTradeResponse trade(StockTradeRequest request) {
        try {
            return executor.submit(() -> {
                var priceRequest = StockPriceRequest.newBuilder()
                        .setStock(request.getStock())
                        .build();
                var currentPrice = this.stockClient.getStockPrice(priceRequest).getPrice();
                // update stock price in original request
                var tradeRequest = request.toBuilder().setTradePrice(currentPrice).build();

                return this.traderClient.tradeStock(tradeRequest);
            }).get(); // unwrap result
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
