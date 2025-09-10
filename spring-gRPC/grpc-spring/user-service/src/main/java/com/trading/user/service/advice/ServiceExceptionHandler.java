package com.trading.user.service.advice;

import com.trading.user.exception.InsufficientBalanceException;
import com.trading.user.exception.InsufficientSharesException;
import com.trading.user.exception.UnknownStockException;
import com.trading.user.exception.UnknownTraderException;
import io.grpc.Status;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;

@GrpcAdvice
public class ServiceExceptionHandler {

    // invoke when UnknownStockException is thrown
    @GrpcExceptionHandler(UnknownStockException.class)
    public Status invalidArguments(UnknownStockException e) {
        return Status.INVALID_ARGUMENT.withDescription(e.getMessage()).withCause(e);
    }

    @GrpcExceptionHandler(UnknownTraderException.class)
    public Status unknownEntities(UnknownTraderException e) {
        return Status.NOT_FOUND.withDescription(e.getMessage()).withCause(e);
    }

    @GrpcExceptionHandler({InsufficientBalanceException.class, InsufficientSharesException.class})
    public Status preconditionFailure(Exception e) {
        return Status.FAILED_PRECONDITION.withDescription(e.getMessage()).withCause(e);
    }
}
