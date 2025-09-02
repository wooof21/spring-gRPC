package com.example.communicationpatterns06;

import com.example.communicationpatterns.AccountBalance;
import com.example.communicationpatterns.BalanceCheckRequest;
import com.example.communicationpatterns.BankServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

@Slf4j
public class GrpcClientMain {

    // gRPC can also generate codes for the client side
    public static void main(String[] args) {

        /**
         * Channel should be mostly private,
         * create once in the beginning when application starts
         * and it will be automatically managed/resued by gRPC
         */
        var channel = ManagedChannelBuilder
                .forAddress("localhost", 6565)
                // disable TLS to simplify the example - for local testing only
                // since HTTP/2 requires secure connection
                .usePlaintext()
                .build();

        /**
         * Stub is a fake implementation of the service
         * It uses channel. Create once and inject wherever needed
         * Singleton / @Bean
         * Thread-safe
         */

//        syncStub(channel);
//        asyncStub(channel);
        futureStub(channel);

    }

    // Blocking stub only supports unary and server-streaming patterns
    private static void syncStub(ManagedChannel channel) {
        var stub = BankServiceGrpc.newBlockingStub(channel);

        var balance = stub.getAccountBalance(
                BalanceCheckRequest.newBuilder()
                        .setAccountNumber(2)
                        .build()
        );

        log.info("Balance: {}", balance.getBalance());
    }

    // Async stub supports all 4 communication patterns
    private static void asyncStub(ManagedChannel channel) {
        var asyncStub = BankServiceGrpc.newStub(channel);

        asyncStub.getAccountBalance(
                BalanceCheckRequest.newBuilder()
                        .setAccountNumber(7)
                        .build(),
                new StreamObserver<>() {
                    @Override
                    public void onNext(AccountBalance value) {
                        log.info("Received balance: {}", value.getBalance());
                    }

                    @Override
                    public void onError(Throwable t) {
                        log.error("Error received: {}", t.getMessage());
                    }

                    @Override
                    public void onCompleted() {
                        log.info("Stream completed");
                    }
                }
        );

        log.info("Request sent");

        // since the call is async, need to sleep the main thread to see the response
        try {
            Thread.sleep(Duration.ofSeconds(1));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // shutdown the channel
        channel.shutdown();
    }

    // Future stub only supports unary and server-streaming patterns
    private static void futureStub(ManagedChannel channel) {
        var futureStub = BankServiceGrpc.newFutureStub(channel);

        var response = futureStub.getAccountBalance(
                BalanceCheckRequest.newBuilder()
                        .setAccountNumber(5)
                        .build()
        );

        try {
            var balance = response.get();
            log.info("Received balance: {}", balance.getBalance());
        } catch (Exception e) {
            log.error("Error received: {}", e.getMessage());
        }

        // shutdown the channel
        channel.shutdown();
    }
}
