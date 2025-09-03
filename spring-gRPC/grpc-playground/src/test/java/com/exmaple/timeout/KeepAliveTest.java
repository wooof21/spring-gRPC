package com.exmaple.timeout;

import com.example.common.GrpcServer;
import com.example.common.GrpcServerKeepAlive;
import com.example.timeout.BalanceCheckRequest;
import com.example.timeout.BankServiceGrpc;
import com.example.timeout09.BankService;
import com.exmaple.common.AbstractChannelTest;
import com.google.common.util.concurrent.Uninterruptibles;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

// keep alive PING & GO AWAY
public class KeepAliveTest extends AbstractChannelTest {

    private static final Logger log = LoggerFactory.getLogger(KeepAliveTest.class);

    private final GrpcServerKeepAlive grpcServer = GrpcServerKeepAlive.create(new BankService());
    private BankServiceGrpc.BankServiceBlockingStub bankBlockingStub;

    @BeforeAll
    public void setup() {
        this.grpcServer.start();
        this.bankBlockingStub = BankServiceGrpc.newBlockingStub(channel);
    }

    // Configure the server with keep alive
    @Test
    public void keepAliveTest() {
        var request = BalanceCheckRequest.newBuilder()
                                         .setAccountNumber(1)
                                         .build();
        var response = this.bankBlockingStub.getAccountBalance(request);
        log.debug("{}", response);

        // blocking the thread for 30 seconds
        Uninterruptibles.sleepUninterruptibly(30, TimeUnit.SECONDS);
    }

    @AfterAll
    public void stop() {
        this.grpcServer.stop();
    }

}
