package com.trading.aggregator.service;

import com.trading.stock.StockPriceRequest;
import com.trading.stock.StockServiceGrpc;
import com.trading.trader.StockTradeRequest;
import com.trading.trader.StockTradeResponse;
import com.trading.trader.TraderServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;

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
                var tradeRequest = request.toBuilder().setPrice(currentPrice).build();

                return this.traderClient.tradeStock(tradeRequest);
            }).get(); // unwrap result
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
