package com.trading.user.v2.service;

import com.trading.trader.v2.*;
import com.trading.user.v2.service.handler.StockTradeRequestHandlerV2;
import com.trading.user.v2.service.handler.TraderInfoRequestHandlerV2;
import io.grpc.stub.StreamObserver;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;


@GrpcService
@Slf4j
@AllArgsConstructor
public class TraderServiceV2 extends TraderServiceGrpc.TraderServiceImplBase {

    private final TraderInfoRequestHandlerV2 traderInfoRequestHandler;
    private final StockTradeRequestHandlerV2 stockTradeRequestHandler;

    @Override
    public void getTraderInformation(TraderInformationRequest request,
                                     StreamObserver<TraderInformation> responseObserver) {
        var traderInfo = traderInfoRequestHandler.getTraderInfo(request);
        responseObserver.onNext(traderInfo);
        responseObserver.onCompleted();
    }

    @Override
    public void tradeStock(StockTradeRequest request,
                           StreamObserver<StockTradeResponse> responseObserver) {
        var response = TradeAction.SELL.equals(request.getAction()) ?
                stockTradeRequestHandler.sell(request) :
                stockTradeRequestHandler.buy(request);

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
