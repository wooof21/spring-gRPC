package com.trading.stock.service;

import com.google.protobuf.Empty;
import com.trading.common.Stock;
import com.trading.stock.v2.PriceUpdate;
import com.trading.stock.v2.StockPriceRequest;
import com.trading.stock.v2.StockPriceResponse;
import com.trading.stock.v2.StockServiceGrpc;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import java.util.function.BiConsumer;

@GrpcService
@Slf4j
@RequiredArgsConstructor
public class StockUpdateService extends StockServiceGrpc.StockServiceImplBase {

    private final StockPriceCache cache;

    @Override
    public void getStockPrice(StockPriceRequest request,
                              StreamObserver<StockPriceResponse> responseObserver) {
        Stock stock = request.getStock();

        var response = StockPriceResponse.newBuilder()
                .setPrice(cache.getPrice(stock))
                .setTimestamp(cache.getLastUpdated(stock).toString())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void getPriceUpdates(Empty request, StreamObserver<PriceUpdate> responseObserver) {

        // ServerCallStreamObserver: get cancellation hooks
        ServerCallStreamObserver<PriceUpdate> serverObserver =
                (ServerCallStreamObserver<PriceUpdate>) responseObserver;

        /**
         * create listeners for each stock
         * every time cache updates the price, it calls listener.accept(stock, newPrice)
         * to push a new PriceUpdate to the gRPC stream (serverObserver.onNext(update))
         *
         * Use BiConsumer to pass callback when stock price updates
         */
        BiConsumer<Stock, Double> listener = (stock, price) -> {
            try {
                var update = PriceUpdate.newBuilder()
                        .setStock(stock)
                        .setPrice(price)
                        .setTimestamp(cache.getLastUpdated(stock).toString())
                        .build();
                serverObserver.onNext(update);
            } catch (Exception e) {
                log.error("Error sending price update to client", e);
            }
        };

        // register listeners for all stocks
        for (Stock stock : Stock.values()) {
            if (stock == Stock.UNKNOWN || stock == Stock.UNRECOGNIZED) continue;
            cache.registerListener(stock, listener);
        }

        // handle client cancellation/disconnection - unregister listeners
        serverObserver.setOnCancelHandler(() -> {
            log.info("Client disconnected.");
            for (Stock stock : Stock.values()) {
                if (stock == Stock.UNKNOWN || stock == Stock.UNRECOGNIZED) continue;
                cache.unregisterListener(stock, listener);
            }
        });

        log.info("New client subscribed for price updates.");

    }
}
