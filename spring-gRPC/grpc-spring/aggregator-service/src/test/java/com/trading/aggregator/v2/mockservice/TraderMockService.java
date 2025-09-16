package com.trading.aggregator.v2.mockservice;

import com.trading.trader.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TraderMockService extends TraderServiceGrpc.TraderServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(TraderMockService.class);

    @Override
    public void getTraderInformation(TraderInformationRequest request,
                                     StreamObserver<TraderInformation> responseObserver) {
        if (request.getTraderId() == 1) {
            var trader = TraderInformation.newBuilder()
                    .setTraderId(1)
                    .setBalance(100)
                    .setName("integration-test")
                    .build();
            responseObserver.onNext(trader);
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(Status.NOT_FOUND.asRuntimeException());
        }
    }

    @Override
    public void tradeStock(StockTradeRequest request,
                           StreamObserver<StockTradeResponse> responseObserver) {
        var response = StockTradeResponse.newBuilder()
                                         .setTraderId(request.getTraderId())
                                         .setStock(request.getStock())
                                         .setAction(request.getAction())
                                         .setPrice(request.getPrice())
                                         .setQuantity(request.getQuantity())
                                         .setTotalPrice(1000)
                                         .setBalance(0)
                                         .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
