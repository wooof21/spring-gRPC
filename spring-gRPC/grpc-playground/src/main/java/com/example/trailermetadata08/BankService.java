package com.example.trailermetadata08;

import com.example.common.GrpcService;
import com.example.trailermetadata08.repository.AccountRepo;
import com.example.trailermetadata08.validator.RequestValidator;
import com.example.validationanderrorhandling.trailermetadata.*;
import com.google.common.util.concurrent.Uninterruptibles;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service("bankServiceWithTrailer")
@Slf4j
public class BankService extends BankServiceGrpc.BankServiceImplBase implements GrpcService {

    @Override
    public void getAccountBalance(BalanceCheckRequest request, StreamObserver<AccountBalance> responseObserver) {
        RequestValidator.validateAccount(request.getAccountNumber())
                        // when above validation returns empty, then check this
                        .or(() -> RequestValidator.isAccountExist(request.getAccountNumber()))
                        .ifPresentOrElse(
                            responseObserver::onError, // if error present
                            // if no error
                            () -> sendAccountBalance(request, responseObserver)
                        );
    }

    private void sendAccountBalance(BalanceCheckRequest request, StreamObserver<AccountBalance> responseObserver) {
        var accountNumber = request.getAccountNumber();
        var balance = AccountRepo.getBalance(accountNumber);
        var accountBalance = AccountBalance.newBuilder()
                               .setAccountNumber(accountNumber)
                               .setBalance(balance)
                               .build();
        responseObserver.onNext(accountBalance);
        responseObserver.onCompleted();
    }

    @Override
    public void withdraw(WithdrawRequest request, StreamObserver<Money> responseObserver) {
        RequestValidator.validateAccount(request.getAccountNumber())
                .or(() -> RequestValidator.isAccountExist(request.getAccountNumber()))
                .or(() -> RequestValidator.isAmountValidate(request.getAmount()))
                .or(() -> RequestValidator.hasSufficientBalance(request.getAmount(),
                        AccountRepo.getBalance(request.getAccountNumber())))
                .ifPresentOrElse(
                        responseObserver::onError,
                        () -> sendMoney(request, responseObserver)
                );
    }

    private void sendMoney(WithdrawRequest request, StreamObserver<Money> responseObserver) {
        var accountNumber = request.getAccountNumber();
        var requestedAmount = request.getAmount();
        for (int i = 0; i < (requestedAmount / 100); i++) {
            var money = Money.newBuilder().setAmount(100).build();
            // mock server error
            // but money for previous 3 iterations will be sent already
            // so when GetAccountBalance of accountNumber - 10, balance will be 9700
            if(i == 3) {
                throw new RuntimeException("Some server error");
            }
            responseObserver.onNext(money);
            log.info("Money sent {}", money);
            AccountRepo.deductMoney(accountNumber, 100);
            Uninterruptibles.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);
        }
        responseObserver.onCompleted();
    }

}
