package com.trading.user.exception;

public class InsufficientBalanceException extends RuntimeException{

    public InsufficientBalanceException(Integer traderId, Integer balance, Integer required) {
        super(String.format("Trader [id=%d] has insufficient balance. " +
                        "Current balance: [%d] - Required: [%d]",
                traderId, balance, required));
    }
}
