package com.trading.user.unit;

import com.trading.common.Stock;
import com.trading.trader.*;
import com.trading.user.service.TraderService;
import com.trading.user.service.handler.StockTradeRequestHandler;
import com.trading.user.service.handler.TraderInfoRequestHandler;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class TraderServiceTest {

    private TraderInfoRequestHandler traderInfoRequestHandler;
    private StockTradeRequestHandler stockTradeRequestHandler;
    private TraderService traderService;

    @BeforeEach
    void setUp() {
        traderInfoRequestHandler = mock(TraderInfoRequestHandler.class);
        stockTradeRequestHandler = mock(StockTradeRequestHandler.class);
        traderService = new TraderService(traderInfoRequestHandler, stockTradeRequestHandler);
    }

    @Test
    void testGetTraderInformation() {
        TraderInformationRequest request = TraderInformationRequest.newBuilder()
                .setTraderId(1)
                .build();

        TraderInformation mockInfo = TraderInformation.newBuilder()
                .setTraderId(1)
                .setName("John")
                .setBalance(1000)
                .build();

        when(traderInfoRequestHandler.getTraderInfo(request)).thenReturn(mockInfo);

        // Mock StreamObserver
        @SuppressWarnings("unchecked")
        StreamObserver<TraderInformation> responseObserver = mock(StreamObserver.class);

        traderService.getTraderInformation(request, responseObserver);

        // Verify handler call
        verify(traderInfoRequestHandler).getTraderInfo(request);

        // Capture the response sent to gRPC
        ArgumentCaptor<TraderInformation> captor = ArgumentCaptor.forClass(TraderInformation.class);
        verify(responseObserver).onNext(captor.capture());
        verify(responseObserver).onCompleted();

        TraderInformation response = captor.getValue();
        assertEquals(1, response.getTraderId());
        assertEquals("John", response.getName());
        assertEquals(1000, response.getBalance());
    }

    @Test
    void testTradeStockBuy() {
        StockTradeRequest request = StockTradeRequest.newBuilder()
                .setTraderId(1)
                .setStock(Stock.APPLE)
                .setQuantity(5)
                .setPrice(100)
                .setAction(TradeAction.BUY)
                .build();

        StockTradeResponse mockResponse = StockTradeResponse.newBuilder()
                .setTraderId(1)
                .setStock(Stock.APPLE)
                .setQuantity(5)
                .setPrice(100)
                .setAction(TradeAction.BUY)
                .setTotalPrice(500)
                .setBalance(500)
                .build();

        when(stockTradeRequestHandler.buy(request)).thenReturn(mockResponse);

        @SuppressWarnings("unchecked")
        StreamObserver<StockTradeResponse> responseObserver = mock(StreamObserver.class);

        traderService.tradeStock(request, responseObserver);

        verify(stockTradeRequestHandler).buy(request);

        ArgumentCaptor<StockTradeResponse> captor = ArgumentCaptor.forClass(StockTradeResponse.class);
        verify(responseObserver).onNext(captor.capture());
        verify(responseObserver).onCompleted();

        StockTradeResponse response = captor.getValue();
        assertEquals(500, response.getBalance());
        assertEquals(TradeAction.BUY, response.getAction());
        assertEquals(5, response.getQuantity());
    }

    @Test
    void testTradeStockSell() {
        StockTradeRequest request = StockTradeRequest.newBuilder()
                .setTraderId(1)
                .setStock(Stock.APPLE)
                .setQuantity(3)
                .setPrice(100)
                .setAction(TradeAction.SELL)
                .build();

        StockTradeResponse mockResponse = StockTradeResponse.newBuilder()
                .setTraderId(1)
                .setStock(Stock.APPLE)
                .setQuantity(3)
                .setPrice(100)
                .setAction(TradeAction.SELL)
                .setTotalPrice(300)
                .setBalance(1300)
                .build();

        when(stockTradeRequestHandler.sell(request)).thenReturn(mockResponse);

        @SuppressWarnings("unchecked")
        StreamObserver<StockTradeResponse> responseObserver = mock(StreamObserver.class);

        traderService.tradeStock(request, responseObserver);

        verify(stockTradeRequestHandler).sell(request);

        ArgumentCaptor<StockTradeResponse> captor = ArgumentCaptor.forClass(StockTradeResponse.class);
        verify(responseObserver).onNext(captor.capture());
        verify(responseObserver).onCompleted();

        StockTradeResponse response = captor.getValue();
        assertEquals(1300, response.getBalance());
        assertEquals(TradeAction.SELL, response.getAction());
        assertEquals(3, response.getQuantity());
    }
}
