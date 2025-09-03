package com.exmaple.timeout;


import com.example.communicationpatterns.*;
import com.exmaple.common.ResponseObserver;
import com.google.common.util.concurrent.Uninterruptibles;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * nginx(http2) load balancing:
 * the channel/connection is configured between client and proxy server (nginx)
 * the proxy server (nginx) and the backend grpc server has its own connection
 *
 * 1. run the 2 bank service instance
 * 2. run 2 instances. 1 on port 6565 and other on 7575
 * 3. start nginx (src/test/resources). nginx listens on port 8585
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LoadBalancingTest {

    private static final Logger log = LoggerFactory.getLogger(LoadBalancingTest.class);

    private BankServiceGrpc.BankServiceBlockingStub bankBlockingStub;
    private BankServiceGrpc.BankServiceStub asyncStub;
    private ManagedChannel channel;

    @BeforeAll
    public void setup() {
        this.channel = ManagedChannelBuilder.forAddress("localhost", 8585)
                                            .usePlaintext()
                                            .build();
        this.bankBlockingStub = BankServiceGrpc.newBlockingStub(channel);
    }

    @Test
    public void loadBalancingTest() {
        for (int i=1; i<10; i++) {
            var request = BalanceCheckRequest.newBuilder()
                                             .setAccountNumber(i)
                                             .build();
            var response = this.bankBlockingStub.getAccountBalance(request);
            log.info("{}", response);
        }
    }

    /**
     * Client Streaming request: the whole streaming is one request
     * only 1 instance process the request
     * unless multiple clients are sending the request
     */
    @Test
    public void clientStreamingLoadBalancingTest() {
        var responseObserver = ResponseObserver.<AccountBalance>create();
        var requestObserver = this.asyncStub.deposit(responseObserver);

        // initial message - account number
        requestObserver.onNext(DepositRequest.newBuilder().setAccountNumber(5).build());

        // sending stream of money
        IntStream.rangeClosed(1, 20)
                .mapToObj(i -> Money.newBuilder().setAmount(100).build())
                .map(m -> DepositRequest.newBuilder().setMoney(m).build())
                .forEach(d -> {
                    Uninterruptibles.sleepUninterruptibly(500, TimeUnit.MILLISECONDS);
                    requestObserver.onNext(d);
                });

        // notifying the server that deposit are done
        requestObserver.onCompleted();

        // at this point, response observer should receive a response
        responseObserver.await();

    }

    @AfterAll
    public void stop() {
        this.channel.shutdownNow();
    }

}
