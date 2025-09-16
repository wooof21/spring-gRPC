package com.trading.stock.service;

import com.trading.common.Stock;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.BiConsumer;

@Component
public class StockPriceCache {

    private final Map<Stock, Double> latestPrices = new ConcurrentHashMap<>();
    private final Map<Stock, Instant> lastUpdated = new ConcurrentHashMap<>();

    // Listeners to push updates when a stock price changes
    // each stock contains the listeners for every client that subscribes to stock price change
    private final Map<Stock, CopyOnWriteArraySet<BiConsumer<Stock, Double>>> listeners =
                                new ConcurrentHashMap<>();

    // initialization
    public StockPriceCache() {
        for (Stock stock : Stock.values()) {
            if (stock == Stock.UNKNOWN || stock == Stock.UNRECOGNIZED) continue;
            latestPrices.put(stock, 0.0); // default price
            lastUpdated.put(stock, Instant.now());
            listeners.put(stock, new CopyOnWriteArraySet<>());
        }
    }

    public void updatePrice(Stock stock, double price) {
        // ignore UNKNOWN and UNRECOGNIZED
        if (stock == Stock.UNKNOWN || stock == Stock.UNRECOGNIZED) return;

        double round = BigDecimal.valueOf(price)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();

        double oldPrice = latestPrices.get(stock);

        // only notify on change
        if (Double.compare(oldPrice, round) != 0) {
            latestPrices.put(stock, round);
            lastUpdated.put(stock, Instant.now());
            // notify all listeners on stock price change
            listeners.get(stock).forEach(listener ->
                                            listener.accept(stock, round));
        }
    }

    public double getPrice(Stock stock) {
        return latestPrices.getOrDefault(stock, 0.0);
    }

    public Instant getLastUpdated(Stock stock) {
        return lastUpdated.get(stock);
    }

    public void registerListener(Stock stock, BiConsumer<Stock, Double> listener) {
        listeners.get(stock).add(listener);
    }

    public void unregisterListener(Stock stock, BiConsumer<Stock, Double> listener) {
        listeners.get(stock).remove(listener);
    }
}
