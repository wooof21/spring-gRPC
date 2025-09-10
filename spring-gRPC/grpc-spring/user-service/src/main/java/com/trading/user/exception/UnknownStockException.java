package com.trading.user.exception;

public class UnknownStockException extends RuntimeException {

    public UnknownStockException() {
        super("Stock not found.");
    }
}
