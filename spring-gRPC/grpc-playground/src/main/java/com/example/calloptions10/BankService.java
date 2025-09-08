package com.example.calloptions10;


import com.example.calloptions.*;
import com.example.calloptions10.repository.AccountRepo;
import com.example.common.GrpcService;
import com.google.common.util.concurrent.Uninterruptibles;
import io.grpc.Context;
import io.grpc.Status;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service("bankServiceCallOptions")
@Slf4j
public class BankService extends BankServiceGrpc.BankServiceImplBase implements GrpcService {


    @Override
    public void getAccountBalance(BalanceCheckRequest request, StreamObserver<AccountBalance> responseObserver) {
        var accountNumber = request.getAccountNumber();
        var balance = AccountRepo.getBalance(accountNumber);

        // to access the user role from context
        // access just by key: Constants.USER_ROLE_KEY.get()
        if(UserRole.STANDARD.equals(Constants.USER_ROLE_KEY.get())) {
            var fee = balance > 0 ? 1 : 0;
            AccountRepo.deductMoney(accountNumber, fee);
            balance = balance - fee;
        }
        var accountBalance = AccountBalance.newBuilder()
                .setAccountNumber(accountNumber)
                .setBalance(balance)
                .build();


        // enable gzip compression on server side -
        // config globally via server interceptor or per call basis
//        ((ServerCallStreamObserver<AccountBalance>) responseObserver).setCompression("gzip");
        // sleep for deadline interceptor test
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
