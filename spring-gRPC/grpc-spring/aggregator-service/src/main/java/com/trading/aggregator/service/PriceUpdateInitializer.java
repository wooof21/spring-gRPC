package com.trading.aggregator.service;

import com.google.protobuf.Empty;
import com.trading.stock.StockServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

// invoke when app starts
@Service
public class PriceUpdateInitializer implements CommandLineRunner {

    @GrpcClient("stock-service")
    private StockServiceGrpc.StockServiceStub client;

    @Autowired
    private StockPriceUpdateListener listener;

    @Override
    public void run(String... args) throws Exception {
        this.client
                // wait for the stock update service to be ready
                .withWaitForReady()
                .getPriceUpdates(Empty.getDefaultInstance(), this.listener);
    }
}
