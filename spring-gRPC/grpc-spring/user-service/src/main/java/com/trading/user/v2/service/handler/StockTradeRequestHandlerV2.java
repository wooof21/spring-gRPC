package com.trading.user.v2.service.handler;

import com.trading.common.Stock;
import com.trading.trader.v2.Holding;
import com.trading.trader.v2.StockTradeRequest;
import com.trading.trader.v2.StockTradeResponse;
import com.trading.trader.v2.TradeAction;
import com.trading.user.exception.InsufficientBalanceException;
import com.trading.user.exception.InsufficientSharesException;
import com.trading.user.exception.UnknownStockException;
import com.trading.user.exception.UnknownTraderException;
import com.trading.user.v2.entity.StockItemV2;
import com.trading.user.v2.repository.StockItemRepoV2;
import com.trading.user.v2.repository.TraderRepoV2;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@AllArgsConstructor
public class StockTradeRequestHandlerV2 {

    private final TraderRepoV2 traderRepoV2;
    private final StockItemRepoV2 stockItemRepoV2;

    @Transactional
    public StockTradeResponse buy(StockTradeRequest request) {
        this.validateStock(request.getStock());
        var trader = traderRepoV2.findById(request.getTraderId())
                .orElseThrow(() -> new UnknownTraderException(request.getTraderId()));

        var totalPrice = request.getQuantity() * request.getTradePrice();
        this.validateBalance(trader.getId(), trader.getBalance(), totalPrice);

        trader.setBalance(trader.getBalance() - totalPrice);

        // If same stock + price exists, increase quantity
        this.stockItemRepoV2.findByTraderIdAndStock(trader.getId(), request.getStock())
                .stream()
                .filter(item -> item.getPurchasePrice().equals(request.getTradePrice()))
                .findFirst()
                .ifPresentOrElse(
                        stock -> stock.setQuantity(stock.getQuantity() + request.getQuantity()),
                        () -> {
                            this.stockItemRepoV2.save(StockItemV2.builder()
                                    .id(UUID.randomUUID().toString())
                                    .traderId(trader.getId())
                                    .stock(request.getStock())
                                    .quantity(request.getQuantity())
                                    .purchasePrice(request.getTradePrice())
                                    .build());
                        }
                );

        return StockTradeResponse.newBuilder()
                .setTraderId(trader.getId())
                .setStock(request.getStock())
                .setQuantity(request.getQuantity())
                .setAction(request.getAction())
                .setTradePrice(request.getTradePrice())
                .setTotalPrice(totalPrice)
                .setBalance(trader.getBalance())
                .addAllUpdatedHoldings(stockItemRepoV2.findAllByTraderId(trader.getId())
                        .stream()
                        .map(this::mapHolding)
                        .toList())
                .build();
    }

    @Transactional
    public StockTradeResponse sell(StockTradeRequest request) {
        this.validateStock(request.getStock());
        var trader = traderRepoV2.findById(request.getTraderId())
                .orElseThrow(() -> new UnknownTraderException(request.getTraderId()));

        var holding = stockItemRepoV2.findById(request.getHoldingId())
                .filter(h -> h.getQuantity() >= request.getQuantity())
                .orElseThrow(() -> new InsufficientSharesException(trader.getId(), request.getStock(),
                        request.getQuantity()));

        var totalPrice = request.getQuantity() * request.getTradePrice();
        trader.setBalance(trader.getBalance() + totalPrice);

        var quantityLeft = holding.getQuantity() - request.getQuantity();
        holding.setQuantity(quantityLeft);
        if (quantityLeft == 0) {
            stockItemRepoV2.delete(holding);
        }

        return StockTradeResponse.newBuilder()
                .setTraderId(trader.getId())
                .setStock(request.getStock())
                .setQuantity(request.getQuantity())
                .setAction(TradeAction.SELL)
                .setTradePrice(request.getTradePrice())
                .setTotalPrice(totalPrice)
                .setBalance(trader.getBalance())
                .addAllUpdatedHoldings(stockItemRepoV2.findAllByTraderId(trader.getId())
                        .stream()
                        .map(this::mapHolding)
                        .toList())
                .build();
    }

    private Holding mapHolding(StockItemV2 item) {
        return Holding.newBuilder()
                .setHoldingId(item.getId())
                .setStock(item.getStock())
                .setQuantity(item.getQuantity())
                .setPurchasePrice(item.getPurchasePrice())
                .build();
    }

    private void validateStock(Stock stock) {
        if (Stock.UNKNOWN.equals(stock)) {
            throw new UnknownStockException();
        }
    }

    private void validateBalance(Integer traderId, Double balance, Double price) {
        if (price > balance) {
            throw new InsufficientBalanceException(traderId, balance.intValue(), price.intValue());
        }
    }
}
