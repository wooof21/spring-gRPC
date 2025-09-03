package com.exmaple.timeout;

import com.example.common.GrpcServer;
import com.example.timeout.BankServiceGrpc;
import com.example.timeout.WithdrawRequest;
import com.example.timeout09.BankService;
import com.exmaple.common.AbstractChannelTest;
import com.google.common.util.concurrent.Uninterruptibles;
import io.grpc.Deadline;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class WaitForReadyTest extends AbstractChannelTest {

    private static final Logger log = LoggerFactory.getLogger(WaitForReadyTest.class);

    private final GrpcServer grpcServer = GrpcServer.create(new BankService());
    private BankServiceGrpc.BankServiceBlockingStub bankBlockingStub;

    @BeforeAll
    public void setup() {
        // let server start after 5 seconds
        Runnable runnable = () -> {
            Uninterruptibles.sleepUninterruptibly(3, TimeUnit.SECONDS);
            this.grpcServer.start();
        };
        Thread.ofVirtual().start(runnable);
        this.bankBlockingStub = BankServiceGrpc.newBlockingStub(channel);
    }

    @Test
    public void blockingDeadlineTest() {
        log.info("Sending the request");
        var request = WithdrawRequest.newBuilder()
                                     .setAccountNumber(1)
                                     .setAmount(500)
                                     .build();
        var ex = Assertions.assertThrows(StatusRuntimeException.class, () -> {
            var iterator = this.bankBlockingStub
                    // client willing to wait for the server to be ready
                    .withWaitForReady()
                    // combine with deadline -> dont wait indefinitely
                    .withDeadline(Deadline.after(8, TimeUnit.SECONDS))
                    .withdraw(request);
            while (iterator.hasNext()) {
                log.info("{}", iterator.next());
            }
        });
        Assertions.assertEquals(Status.Code.DEADLINE_EXCEEDED, ex.getStatus().getCode());
    }

    @AfterAll
    public void stop() {
        this.grpcServer.stop();
    }

}
