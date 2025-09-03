package com.example.timeout09;

import com.example.common.GrpcService;
import com.example.timeout.*;
import com.example.timeout09.repository.AccountRepo;
import com.google.common.util.concurrent.Uninterruptibles;
import io.grpc.Context;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service("bankServiceTimeout")
@Slf4j
public class BankService extends BankServiceGrpc.BankServiceImplBase implements GrpcService {


    @Override
    public void getAccountBalance(BalanceCheckRequest request, StreamObserver<AccountBalance> responseObserver) {
        var accountNumber = request.getAccountNumber();
        var balance = AccountRepo.getBalance(accountNumber);
        var accountBalance = AccountBalance.newBuilder()
                                           .setAccountNumber(accountNumber)
                                           .setBalance(balance)
                                           .build();
        // server slow processing
        Uninterruptibles.sleepUninterruptibly(3, TimeUnit.SECONDS);
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

        /**
         * Context: manage lifecycle of the rpc
         *    - server process multiple rpc requests
         *    - each rpc has its own context
         *
         *  * Context.current() -> get the current rpc context
         *  * isCancelled() -> check if the rpc is cancelled (by client or deadline exceeded)
         *
         *  Without the condition check, server will keep processing the request
         *  even the client has cancelled the request
         */
        for (int i = 0; i < (requestedAmount / 100) && !Context.current().isCancelled(); i++) {
            var money = Money.newBuilder().setAmount(100).build();
            responseObserver.onNext(money);
            log.info("Money sent: {}", money);
            AccountRepo.deductMoney(accountNumber, 100);
            // server slow processing
            Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
        }
        log.info("Streaming completed");
        responseObserver.onCompleted();
    }

}
