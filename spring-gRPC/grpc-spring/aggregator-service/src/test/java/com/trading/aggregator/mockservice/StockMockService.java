package com.trading.aggregator.mockservice;

import com.google.common.util.concurrent.Uninterruptibles;
import com.google.protobuf.Empty;
import com.trading.common.Stock;
import com.trading.stock.PriceUpdate;
import com.trading.stock.StockPriceRequest;
import com.trading.stock.StockPriceResponse;
import com.trading.stock.StockServiceGrpc;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class StockMockService extends StockServiceGrpc.StockServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(StockMockService.class);

    @Override
    public void getStockPrice(StockPriceRequest request, StreamObserver<StockPriceResponse> responseObserver) {
        var response = StockPriceResponse.newBuilder().setPrice(15).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getPriceUpdates(Empty request, StreamObserver<PriceUpdate> responseObserver) {
        Uninterruptibles.sleepUninterruptibly(3, TimeUnit.SECONDS);
        for (int i=1; i<=5; i++) {
            var priceUpdate = PriceUpdate.newBuilder().setPrice(i).setStock(Stock.APPLE).build();
            log.info("{}", priceUpdate);
            responseObserver.onNext(priceUpdate);
        }
        responseObserver.onCompleted();
    }
}
