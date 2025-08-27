package com.example.communicationpatterns.requesthandlers;

import com.example.communicationpatterns.AccountBalance;
import com.example.communicationpatterns.TransferRequest;
import com.example.communicationpatterns.TransferResponse;
import com.example.communicationpatterns.TransferStatus;
import com.example.communicationpatterns.repos.AccountRepo;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TransferRequestHandler implements StreamObserver<TransferRequest> {

    private final StreamObserver<TransferResponse> responseObserver;

    public TransferRequestHandler(StreamObserver<TransferResponse> responseObserver) {
        this.responseObserver = responseObserver;
    }

    @Override
    public void onNext(TransferRequest transferRequest) {
        var status = this.transfer(transferRequest);
        var response = TransferResponse.newBuilder()
                           .setFromAccount(this.toAccountBalance(transferRequest.getFromAccount()))
                           .setToAccount(this.toAccountBalance(transferRequest.getToAccount()))
                           .setStatus(status)
                           .build();
        this.responseObserver.onNext(response);
    }

    @Override
    public void onError(Throwable throwable) {
        log.error("Client error: {}", throwable.getMessage());
    }

    @Override
    public void onCompleted() {
        log.info("Transfer request stream completed");
        this.responseObserver.onCompleted();
    }

    private TransferStatus transfer(TransferRequest request) {
        var amount = request.getAmount();
        var fromAccount = request.getFromAccount();
        var toAccount = request.getToAccount();
        var status = TransferStatus.REJECTED;
        if (AccountRepo.getBalance(fromAccount) >= amount && (fromAccount != toAccount)) {
            AccountRepo.deductMoney(fromAccount, amount);
            AccountRepo.depositMoney(toAccount, amount);
            status = TransferStatus.COMPLETED;
        }
        return status;
    }

    private AccountBalance toAccountBalance(int accountNumber) {
        return AccountBalance.newBuilder()
                             .setAccountNumber(accountNumber)
                             .setBalance(AccountRepo.getBalance(accountNumber))
                             .build();
    }

}
