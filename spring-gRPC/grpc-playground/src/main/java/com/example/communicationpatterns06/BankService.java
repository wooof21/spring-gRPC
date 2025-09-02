package com.example.communicationpatterns06;

import com.example.common.GrpcService;
import com.example.communicationpatterns.*;
import com.example.communicationpatterns06.repos.AccountRepo;
import com.example.communicationpatterns06.requesthandlers.DepositRequestHandler;
import com.google.common.util.concurrent.Uninterruptibles;
import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class BankService extends BankServiceGrpc.BankServiceImplBase implements GrpcService {

    // override the grpc method
    @Override
    public void getAccountBalance(BalanceCheckRequest request,
                                  StreamObserver<AccountBalance> responseObserver) {

        var accountNumber = request.getAccountNumber();
        log.info("Request - Account Number: {}", accountNumber);
        var balance = AccountRepo.getBalance(accountNumber);
        var accountBalance = AccountBalance.newBuilder()
                .setAccountNumber(accountNumber)
                .setBalance(balance)
                .build();

        // send the accountBalance to the client
        responseObserver.onNext(accountBalance);
        // successfully get the accountBalance - complete the RPC call
        responseObserver.onCompleted();
    }

    @Override
    public void getAllAccounts(Empty request,
                               StreamObserver<AllAccountsResponse> responseObserver) {
        var accounts = AccountRepo.getAllAccounts()
                .entrySet()
                .stream()
                .map(e -> AccountBalance.newBuilder()
                        .setAccountNumber(e.getKey())
                        .setBalance(e.getValue())
                        .build())
                .toList();

        var response = AllAccountsResponse.newBuilder()
                .addAllAccounts(accounts)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    // server streaming
    @Override
    public void withdraw(WithdrawRequest request, StreamObserver<Money> responseObserver) {

        var accountNumber = request.getAccountNumber();
        var totalAmount = request.getAmount();
        var accountBalance = AccountRepo.getBalance(accountNumber);

        if(totalAmount > accountBalance) {
            responseObserver.onCompleted();
            return;
        }

        // make each withdrawal of $10, run (totalAmount)/10 times
        for(int i=0; i< (totalAmount)/10; i++) {
            var remainingBalance = AccountRepo.getBalance(accountNumber);
            var money = Money.newBuilder().setAmount(10)
                    .setRemainingBalance(remainingBalance).build();
            responseObserver.onNext(money);
            AccountRepo.deductMoney(accountNumber, 10);
            log.info("Deducted $10 from account number: {}. Remaining balance: {}",
                    accountNumber, remainingBalance);
            // make the withdrawal take some time
            Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
        }

        responseObserver.onCompleted();
    }

    // client streaming
    @Override
    public StreamObserver<DepositRequest> deposit(StreamObserver<AccountBalance> responseObserver) {
        return new DepositRequestHandler(responseObserver);
    }
}
