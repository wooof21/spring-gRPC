package com.trading.user.exception;

public class UnknownTraderException extends RuntimeException {

    public UnknownTraderException(Integer traderId) {
        super(String.format("Trader [id=%d] not found. " ,traderId));
    }
}
