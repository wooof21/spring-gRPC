package com.example.communicationpatterns06.requesthandlers;

import com.example.communicationpatterns.AccountBalance;
import com.example.communicationpatterns.DepositRequest;
import com.example.communicationpatterns06.repos.AccountRepo;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DepositRequestHandler implements StreamObserver<DepositRequest> {

    private final StreamObserver<AccountBalance> responseObserver;
    private int accountNumber;
    private int totalDepositedAmount;

    public DepositRequestHandler(StreamObserver<AccountBalance> responseObserver) {
        this.responseObserver = responseObserver;

        /**
         * Don't rely on POSTMAN for cancellation testing -
         * Use grpcurl or a gRPC client SDK instead of Postman to properly simulate cancellation.
         *
         * When test in POSTMAN: and cancel the request, the onError() is not called
         *
         *  - In gRPC, cancellation is not always propagated to the server automatically,
         *      especially when test via Postman or tools that don't fully implement gRPC cancellation semantics.
         *  - The onError() callback in the server-side StreamObserver is only invoked if:
         *      * The client cancels the call properly (sends a CANCEL signal, not just closes the connection abruptly).
         *      * Or if there is a transport-level error (e.g., connection dropped mid-stream).
         *  - When simply stop Postman’s request, it closes the HTTP/2 stream, but the server may not always
         *      map that directly to a gRPC CANCELLED signal → so server observer won’t see onError().
         *
         *   - The `onCompleted()` method will still be called if the client closes the connection gracefully,
         *      which leads to the `totalDepositedAmount` was committed instead of rolled back.
         *   - Since `onCompleted()` is always called when the server thinks the stream finished gracefully,
         *      and cancellation + close can sometimes look like a “clean finish” if don’t guard it.
         *   - gRPC’s internal StreamObserver pipeline doesn’t automatically know that cancellation
         *      means want to skip your commit logic
         *      * setOnCancelHandler() runs immediately when the client disconnects.
         *      * But later, gRPC still drives onCompleted() because the server-side observer sees the stream closed.
         *      * This is why money deposit still commits instead of rolling back.
         *   - Need a flag to distinguish normal completion vs cancellation:
         */
    }

    @Override
    public void onNext(DepositRequest depositRequest) {
        log.info("Deposit money request: {}", depositRequest);
        switch (depositRequest.getRequestCase()) {
            case ACCOUNT_NUMBER -> this.accountNumber = depositRequest.getAccountNumber();
            case MONEY -> totalDepositedAmount += depositRequest.getMoney().getAmount();
            case REQUEST_NOT_SET -> log.warn("Request not set");
        }
    }

    // roll back the transaction if there is an error
    @Override
    public void onError(Throwable throwable) {
        totalDepositedAmount = 0;
        log.info("Client error: {}", throwable.getMessage());
    }

    // commit the transaction if everything is fine
    @Override
    public void onCompleted() {

        AccountRepo.depositMoney(this.accountNumber, totalDepositedAmount);
        var accountBalance = AccountBalance.newBuilder()
                                           .setAccountNumber(this.accountNumber)
                                           .setBalance(AccountRepo.getBalance(this.accountNumber))
                                           .build();
        this.responseObserver.onNext(accountBalance);
        this.responseObserver.onCompleted();
    }
}
