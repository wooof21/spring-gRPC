package com.exmaple.timeout;

import com.example.common.GrpcServer;
import com.example.timeout.BalanceCheckRequest;
import com.example.timeout.BankServiceGrpc;
import com.example.timeout09.BankService;
import com.exmaple.common.AbstractChannelTest;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// lazy channel creation - channel created lazily when the first RPC is made
// if there is no server to connect to, the RPC will fail with UNAVAILABLE
public class LazyChannelTest extends AbstractChannelTest {

    private static final Logger log = LoggerFactory.getLogger(LazyChannelTest.class);

    private final GrpcServer grpcServer = GrpcServer.create(new BankService());
    private BankServiceGrpc.BankServiceBlockingStub bankBlockingStub;

    @BeforeAll
    public void setup() {
      //  this.grpcServer.start();
        this.bankBlockingStub = BankServiceGrpc.newBlockingStub(channel);
    }

    @Test
    public void lazyChannelCreation() {
        var ex = Assertions.assertThrows(StatusRuntimeException.class, () -> {
            var request = BalanceCheckRequest.newBuilder()
                                             .setAccountNumber(1)
                                             .build();
            // the first rpc call establishes the channel/connection
            // if there is no server to connect to, the RPC will fail with UNAVAILABLE
            var response = this.bankBlockingStub.getAccountBalance(request);
            log.info("{}", response);
        });
        Assertions.assertEquals(Status.Code.UNAVAILABLE, ex.getStatus().getCode());
    }

    @AfterAll
    public void stop() {
        this.grpcServer.stop();
    }

}
