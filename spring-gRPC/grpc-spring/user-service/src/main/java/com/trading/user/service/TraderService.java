package com.trading.user.service;

import com.trading.trader.*;
import com.trading.user.service.handler.StockTradeRequestHandler;
import com.trading.user.service.handler.TraderInfoRequestHandler;
import io.grpc.stub.StreamObserver;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@AllArgsConstructor
@Slf4j
public class TraderService extends TraderServiceGrpc.TraderServiceImplBase {

    private final TraderInfoRequestHandler traderInfoRequestHandler;
    private final StockTradeRequestHandler stockTradeRequestHandler;

    @Override
    public void getTraderInformation(TraderInformationRequest request,
                                     StreamObserver<TraderInformation> responseObserver) {
        log.info("Trader request: {}", request);
        var traderInfo = traderInfoRequestHandler.getTraderInfo(request);
        responseObserver.onNext(traderInfo);
        responseObserver.onCompleted();
    }

    @Override
    public void tradeStock(StockTradeRequest request, StreamObserver<StockTradeResponse> responseObserver) {
        log.info("Trade stock request: {}", request);
        var response = TradeAction.SELL.equals(request.getAction()) ?
                stockTradeRequestHandler.sell(request) :
                stockTradeRequestHandler.buy(request);

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
