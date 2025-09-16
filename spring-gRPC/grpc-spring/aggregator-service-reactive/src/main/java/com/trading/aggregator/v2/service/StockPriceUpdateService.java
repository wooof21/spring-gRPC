package com.trading.aggregator.v2.service;

import com.google.protobuf.Empty;
import com.trading.aggregator.v2.model.PriceUpdateModel;
import com.trading.stock.v2.PriceUpdate;
import com.trading.stock.v2.StockServiceGrpc;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.util.concurrent.ExecutorService;

@Service
@Slf4j
@RequiredArgsConstructor
public class StockPriceUpdateService {

    @GrpcClient("stock-service")
    private StockServiceGrpc.StockServiceStub  client;
    private final ExecutorService executor;

    public Flux<PriceUpdateModel> getPriceUpdates() {
        return Flux.create(emitter -> {
            executor.submit(() -> {
                /**
                 * Every time the stock-service pushes a new price, onNext is invoked.
                 *
                 * emitter.next() then feeds the WebFlux Flux, which streams it to the frontend clients.
                 *
                 * When the server closes the stream, onCompleted is called, ending the Flux.
                 */
                StreamObserver<PriceUpdate> responseObserver = new StreamObserver<>() {
                    @Override
                    public void onNext(PriceUpdate update) {
                        emitter.next(new PriceUpdateModel(update.getStock().name(),
                                update.getPrice()));
                    }

                    @Override
                    public void onError(Throwable t) {
                        emitter.error(t);
                    }

                    @Override
                    public void onCompleted() {
                        emitter.complete();
                    }
                };

                client.getPriceUpdates(Empty.getDefaultInstance(), responseObserver);
            }, "stock-update-virtual-thread");
        },
        // Backpressure-aware for multiple clients.
        FluxSink.OverflowStrategy.LATEST);
    }
}
