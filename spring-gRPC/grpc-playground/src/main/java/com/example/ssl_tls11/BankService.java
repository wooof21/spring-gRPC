package com.example.ssl_tls11;

import com.example.common.GrpcService;
import com.example.ssl_tls.*;
import com.example.ssl_tls11.repository.AccountRepo;
import com.google.common.util.concurrent.Uninterruptibles;
import io.grpc.Context;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class BankService extends BankServiceGrpc.BankServiceImplBase implements GrpcService {

    private static final Logger log = LoggerFactory.getLogger(BankService.class);

    @Override
    public void getAccountBalance(BalanceCheckRequest request, StreamObserver<AccountBalance> responseObserver) {
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
        var accountNumber = request.getAccountNumber();
        var requestedAmount = request.getAmount();
        var accountBalance = AccountRepo.getBalance(accountNumber);

        if (requestedAmount > accountBalance) {
            responseObserver.onError(Status.FAILED_PRECONDITION.asRuntimeException());
            return;
        }

        for (int i=0; i<(requestedAmount/10) && !Context.current().isCancelled(); i++) {
            var money = Money.newBuilder().setAmount(10).build();
            responseObserver.onNext(money);
            log.info("Money sent: {}", money);
            AccountRepo.deductMoney(accountNumber, 10);
            Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
        }
        log.info("Streaming completed");
        responseObserver.onCompleted();
    }

}
