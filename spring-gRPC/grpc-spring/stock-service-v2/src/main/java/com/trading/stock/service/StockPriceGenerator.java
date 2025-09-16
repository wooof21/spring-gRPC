package com.trading.stock.service;

import com.trading.common.Stock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Random;

@Component
@Slf4j
@RequiredArgsConstructor
public class StockPriceGenerator {

    private final StockPriceCache cache;
    private final Random random = new Random();

    // skip UNKNOWN stock to update
    private final Stock[] stocks = Arrays.stream(Stock.values())
            .filter(s -> s != Stock.UNKNOWN && s != Stock.UNRECOGNIZED)
            .toArray(Stock[]::new);

    // generate random stock prices every 5s
    @Scheduled(fixedRate = 5000)
    public void updatePrices() {
        for (Stock stock : stocks) {
            double oldPrice = cache.getPrice(stock);
            double newPrice = oldPrice + (random.nextDouble() - 0.5) * 5;
            // Ensure price >= 1
            newPrice = Math.max(1, newPrice);
            double round = BigDecimal.valueOf(newPrice)
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();
            cache.updatePrice(stock, round);
            log.info("Updated stock price: [{}] -> [{}]", stock, round);
        }
    }
}
