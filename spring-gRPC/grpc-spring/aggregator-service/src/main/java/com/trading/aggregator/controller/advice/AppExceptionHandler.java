package com.trading.aggregator.controller.advice;

import io.grpc.StatusRuntimeException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class AppExceptionHandler {

    @ExceptionHandler(StatusRuntimeException.class)
    public ResponseEntity<String> statusRuntimeException(StatusRuntimeException ex) {
        return switch (ex.getStatus().getCode()) {
            case INVALID_ARGUMENT, FAILED_PRECONDITION ->
                    ResponseEntity.badRequest().body(ex.getStatus().getDescription());
            case NOT_FOUND -> ResponseEntity.notFound().build();
            case null, default -> ResponseEntity.internalServerError().body("Internal server error");
        };
    }
}
