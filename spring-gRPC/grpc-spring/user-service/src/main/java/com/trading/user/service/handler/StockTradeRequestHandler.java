package com.trading.user.service.handler;

import com.trading.common.Stock;
import com.trading.trader.StockTradeRequest;
import com.trading.trader.StockTradeResponse;
import com.trading.user.entity.StockItem;
import com.trading.user.exception.InsufficientBalanceException;
import com.trading.user.exception.InsufficientSharesException;
import com.trading.user.exception.UnknownStockException;
import com.trading.user.exception.UnknownTraderException;
import com.trading.user.repository.StockItemRepo;
import com.trading.user.repository.TraderRepo;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class StockTradeRequestHandler {

    private final TraderRepo traderRepo;
    private final StockItemRepo stockItemRepo;


    @Transactional
    public StockTradeResponse buy(StockTradeRequest request) {
        // validation
        this.validateStock(request.getStock());
        var trader = traderRepo.findById(request.getTraderId())
                .orElseThrow(() -> new UnknownTraderException(request.getTraderId()));

        var totalPrice = request.getQuantity() * request.getPrice();
        this.validateBalance(trader.getId(), trader.getBalance(), totalPrice);

        trader.setBalance(trader.getBalance() - totalPrice);
        // update/insert trader holdings table
        this.stockItemRepo.findByTraderIdAndStock(trader.getId(), request.getStock())
                .ifPresentOrElse(
                        stock -> stock.setQuantity(stock.getQuantity() + request.getQuantity()),
                        () -> this.stockItemRepo.save(StockItem.builder()
                                        .traderId(request.getTraderId())
                                        .stock(request.getStock())
                                        .quantity(request.getQuantity())
                                .build()));


        return StockTradeResponse.newBuilder()
                .setTraderId(request.getTraderId())
                .setPrice(request.getPrice())
                .setStock(request.getStock())
                .setQuantity(request.getQuantity())
                .setAction(request.getAction())
                .setTotalPrice(totalPrice)
                .setBalance(trader.getBalance())
                .build();
    }

    @Transactional
    public StockTradeResponse sell(StockTradeRequest request) {
        // validation
        this.validateStock(request.getStock());
        var trader = traderRepo.findById(request.getTraderId())
                .orElseThrow(() -> new UnknownTraderException(request.getTraderId()));

        var stockItem = this.stockItemRepo.findByTraderIdAndStock(trader.getId(), request.getStock())
                .filter(stock -> stock.getQuantity() >= request.getQuantity())
                .orElseThrow(() -> new InsufficientSharesException(trader.getId(), request.getStock(),
                        request.getQuantity()));

        var totalPrice = request.getQuantity() * request.getPrice();
        trader.setBalance(trader.getBalance() + totalPrice);
        var quantityLeft = stockItem.getQuantity() - request.getQuantity();
        stockItem.setQuantity(quantityLeft);
        if(quantityLeft == 0) {
            this.stockItemRepo.delete(stockItem);
        }


        return StockTradeResponse.newBuilder()
                .setTraderId(request.getTraderId())
                .setPrice(request.getPrice())
                .setStock(request.getStock())
                .setQuantity(request.getQuantity())
                .setAction(request.getAction())
                .setTotalPrice(totalPrice)
                .setBalance(trader.getBalance())
                .build();
    }

    private void validateStock(Stock stock) {
        if(Stock.UNKNOWN.equals(stock)) {
            throw new UnknownStockException();
        }
    }

    private void validateBalance(Integer traderId, Integer balance, Integer price) {
        if(price > balance) {
            throw new InsufficientBalanceException(traderId, balance, price);
        }
    }
}
