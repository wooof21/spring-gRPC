package com.trading.aggregator.v2.service;

import com.trading.trader.v2.TraderInformation;
import com.trading.trader.v2.TraderInformationRequest;
import com.trading.trader.v2.TraderServiceGrpc;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;

@Service
@Slf4j
public class TraderService {

    @GrpcClient("trader-service")
    private TraderServiceGrpc.TraderServiceBlockingStub client;
    @Autowired
    private ExecutorService executor;

    public TraderInformation getTraderInformation(Integer traderId) {
        var request = TraderInformationRequest.newBuilder()
                .setTraderId(traderId)
                .build();
        try {
            return executor.submit(() -> this.client.getTraderInformation(request)).get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
