package com.trading.aggregator.service;

import com.trading.trader.TraderInformation;
import com.trading.trader.TraderInformationRequest;
import com.trading.trader.TraderServiceGrpc;
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
            return executor.submit(() -> this.client.getTraderInformation(request))
                    .get(); // unwrap result
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
