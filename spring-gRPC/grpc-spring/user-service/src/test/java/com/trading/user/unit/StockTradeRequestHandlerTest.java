package com.trading.user.unit;

import com.trading.common.Stock;
import com.trading.trader.StockTradeRequest;
import com.trading.trader.StockTradeResponse;
import com.trading.trader.TradeAction;
import com.trading.user.entity.StockItem;
import com.trading.user.entity.Trader;
import com.trading.user.exception.InsufficientBalanceException;
import com.trading.user.exception.InsufficientSharesException;
import com.trading.user.exception.UnknownStockException;
import com.trading.user.repository.StockItemRepo;
import com.trading.user.repository.TraderRepo;
import com.trading.user.service.handler.StockTradeRequestHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StockTradeRequestHandlerTest {

    private TraderRepo traderRepo;
    private StockItemRepo stockItemRepo;
    private StockTradeRequestHandler handler;

    @BeforeEach
    void setUp() {
        traderRepo = mock(TraderRepo.class);
        stockItemRepo = mock(StockItemRepo.class);
        handler = new StockTradeRequestHandler(traderRepo, stockItemRepo);
    }

    @Test
    void testBuy_success() {
        Trader trader = new Trader(1, "John", 1000);
        when(traderRepo.findById(1)).thenReturn(Optional.of(trader));
        when(stockItemRepo.findByTraderIdAndStock(1, Stock.APPLE))
                .thenReturn(Optional.empty());
        when(stockItemRepo.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StockTradeRequest request = StockTradeRequest.newBuilder()
                .setTraderId(1)
                .setStock(Stock.APPLE)
                .setQuantity(5)
                .setPrice(100)
                .setAction(TradeAction.BUY)
                .build();

        StockTradeResponse response = handler.buy(request);

        assertEquals(500, response.getTotalPrice());
        assertEquals(500, response.getBalance());
        verify(stockItemRepo).save(any(StockItem.class));
    }

    @Test
    void testBuy_insufficientBalance() {
        Trader trader = new Trader(1, "John", 100);
        when(traderRepo.findById(1)).thenReturn(Optional.of(trader));

        StockTradeRequest request = StockTradeRequest.newBuilder()
                .setTraderId(1)
                .setStock(Stock.APPLE)
                .setQuantity(5)
                .setPrice(100)
                .setAction(TradeAction.BUY)
                .build();

        assertThrows(InsufficientBalanceException.class, () -> handler.buy(request));
    }

    @Test
    void testSell_success() {
        Trader trader = new Trader(1, "John", 1000);
        StockItem stockItem = StockItem.builder()
                .traderId(1)
                .stock(Stock.APPLE)
                .quantity(5)
                .build();

        when(traderRepo.findById(1)).thenReturn(Optional.of(trader));
        when(stockItemRepo.findByTraderIdAndStock(1, Stock.APPLE))
                .thenReturn(Optional.of(stockItem));

        StockTradeRequest request = StockTradeRequest.newBuilder()
                .setTraderId(1)
                .setStock(Stock.APPLE)
                .setQuantity(3)
                .setPrice(100)
                .setAction(TradeAction.SELL)
                .build();

        StockTradeResponse response = handler.sell(request);

        assertEquals(3, response.getQuantity());
        assertEquals(1300, response.getBalance());
        assertEquals(2, stockItem.getQuantity());
    }

    @Test
    void testSell_insufficientShares() {
        Trader trader = new Trader(1, "John", 1000);
        StockItem stockItem = StockItem.builder()
                .traderId(1)
                .stock(Stock.APPLE)
                .quantity(2)
                .build();

        when(traderRepo.findById(1)).thenReturn(Optional.of(trader));
        when(stockItemRepo.findByTraderIdAndStock(1, Stock.APPLE))
                .thenReturn(Optional.of(stockItem));

        StockTradeRequest request = StockTradeRequest.newBuilder()
                .setTraderId(1)
                .setStock(Stock.APPLE)
                .setQuantity(3)
                .setPrice(100)
                .setAction(TradeAction.SELL)
                .build();

        assertThrows(InsufficientSharesException.class, () -> handler.sell(request));
    }

    @Test
    void testBuy_unknownStock() {
        StockTradeRequest request = StockTradeRequest.newBuilder()
                .setTraderId(1)
                .setStock(Stock.UNKNOWN)
                .setQuantity(1)
                .setPrice(1)
                .setAction(TradeAction.BUY)
                .build();

        assertThrows(UnknownStockException.class, () -> handler.buy(request));
    }
}
