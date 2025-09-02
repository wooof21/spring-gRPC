package com.example.validationanderrorhandling07.validator;


import com.example.validationanderrorhandling07.repository.AccountRepo;
import io.grpc.Status;

import java.util.Optional;

public class RequestValidator {

    public static Optional<Status> validateAccount(int accountNumber) {
        //valid account
        if(accountNumber > 0 && accountNumber < 11) {
            return Optional.empty();
        }
        //invalid with status and description
        return Optional.of(Status.INVALID_ARGUMENT
                .withDescription("Account number should be between 1 and 10"));
    }

    public static Optional<Status> isAccountExist(int accountNumber) {
        if(AccountRepo.isAccountExist(accountNumber)) {
            return Optional.empty();
        }
        return Optional.of(Status.NOT_FOUND
                .withDescription("Account number - " + accountNumber + " - not found"));
    }

    // amount divisible by 100
    public static Optional<Status> isAmountValidate(int amount) {
        if(amount > 0 && amount % 100 == 0) {
            return Optional.empty();
        }
        return Optional.of(Status.INVALID_ARGUMENT
                .withDescription("Requested amount should be divisible by 100"));
    }

    public static Optional<Status> hasSufficientBalance(int amount, int balance) {
        if(amount <= balance) {
            return Optional.empty();
        }
        return Optional.of(Status.FAILED_PRECONDITION
                .withDescription("Insufficient balance"));
    }
}
