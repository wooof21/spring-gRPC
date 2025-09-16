package com.trading.user.v2.service.handler;

import com.trading.trader.v2.Holding;
import com.trading.trader.v2.TraderInformation;
import com.trading.trader.v2.TraderInformationRequest;
import com.trading.user.exception.UnknownTraderException;
import com.trading.user.v2.repository.StockItemRepoV2;
import com.trading.user.v2.repository.TraderRepoV2;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class TraderInfoRequestHandlerV2 {

    private final TraderRepoV2 traderRepoV2;
    private final StockItemRepoV2 stockItemRepoV2;

    public TraderInformation getTraderInfo(TraderInformationRequest request) {
        var trader = traderRepoV2.findById(request.getTraderId())
                .orElseThrow(() -> new UnknownTraderException(request.getTraderId()));

        var holdings = stockItemRepoV2.findAllByTraderId(trader.getId())
                .stream()
                .map(item -> Holding.newBuilder()
                        .setHoldingId(item.getId())
                        .setStock(item.getStock())
                        .setQuantity(item.getQuantity())
                        .setPurchasePrice(item.getPurchasePrice())
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
