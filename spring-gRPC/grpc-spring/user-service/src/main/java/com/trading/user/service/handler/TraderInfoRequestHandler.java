package com.trading.user.service.handler;

import com.trading.trader.Holding;
import com.trading.trader.TraderInformation;
import com.trading.trader.TraderInformationRequest;
import com.trading.user.exception.UnknownTraderException;
import com.trading.user.repository.StockItemRepo;
import com.trading.user.repository.TraderRepo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

// handle trader info
@Component
@AllArgsConstructor
public class TraderInfoRequestHandler {

    private final TraderRepo traderRepo;
    private final StockItemRepo stockItemRepo;

    public TraderInformation getTraderInfo(TraderInformationRequest request) {
        var trader = traderRepo.findById(request.getTraderId())
                .orElseThrow(() -> new UnknownTraderException(request.getTraderId()));
        var stockItems = stockItemRepo.findAllByTraderId(request.getTraderId());
        var holdings = stockItems.stream()
                .map(item -> Holding.newBuilder()
                        .setStock(item.getStock())
                        .setQuantity(item.getQuantity())
                        .build())
                .toList();
        return TraderInformation.newBuilder()
                .setTraderId(trader.getId())
                .setName(trader.getName())
                .setBalance(trader.getBalance())
                .addAllHoldings(holdings)
                .build();

    }
}
