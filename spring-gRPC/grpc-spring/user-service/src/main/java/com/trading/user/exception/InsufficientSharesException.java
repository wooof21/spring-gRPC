package com.trading.user.exception;

import com.trading.common.Stock;

public class InsufficientSharesException extends RuntimeException {
    public InsufficientSharesException(Integer traderId, Stock stock, Integer requested) {
        super(String.format("Trader [%d] has insufficient shares for stock [%s] - trading [%d]", traderId, stock, requested));
    }
}
