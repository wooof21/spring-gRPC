package com.trading.user.unit;

import com.trading.common.Stock;
import com.trading.trader.Holding;
import com.trading.trader.TraderInformation;
import com.trading.trader.TraderInformationRequest;
import com.trading.user.entity.StockItem;
import com.trading.user.entity.Trader;
import com.trading.user.exception.UnknownTraderException;
import com.trading.user.repository.StockItemRepo;
import com.trading.user.repository.TraderRepo;
import com.trading.user.service.handler.TraderInfoRequestHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TraderInfoRequestHandlerTest {

    private TraderRepo traderRepo;
    private StockItemRepo stockItemRepo;
    private TraderInfoRequestHandler handler;

    @BeforeEach
    void setUp() {
        traderRepo = mock(TraderRepo.class);
        stockItemRepo = mock(StockItemRepo.class);
        handler = new TraderInfoRequestHandler(traderRepo, stockItemRepo);
    }

    @Test
    void testGetTraderInfo_success() {
        Trader trader = new Trader(1, "John", 1000);
        when(traderRepo.findById(1)).thenReturn(Optional.of(trader));

        StockItem item = StockItem.builder()
                .traderId(1)
                .stock(Stock.APPLE)
                .quantity(10)
                .build();
        when(stockItemRepo.findAllByTraderId(1)).thenReturn(List.of(item));

        TraderInformationRequest request = TraderInformationRequest.newBuilder().setTraderId(1).build();
        TraderInformation info = handler.getTraderInfo(request);

        assertEquals(1, info.getTraderId());
        assertEquals("John", info.getName());
        assertEquals(1000, info.getBalance());
        assertEquals(1, info.getHoldingsCount());

        Holding holding = info.getHoldings(0);
        assertEquals(Stock.APPLE, holding.getStock());
        assertEquals(10, holding.getQuantity());
    }

    @Test
    void testGetTraderInfo_unknownTrader() {
        when(traderRepo.findById(1)).thenReturn(Optional.empty());

        TraderInformationRequest request = TraderInformationRequest.newBuilder().setTraderId(1).build();

        assertThrows(UnknownTraderException.class, () -> handler.getTraderInfo(request));
    }
}
