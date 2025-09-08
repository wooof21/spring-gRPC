package com.exmaple.calloptions;

import com.example.calloptions.BankServiceGrpc;
import com.example.calloptions10.BankService;
import com.example.calloptions10.serverinterceptor.GzipResponseInterceptor;
import com.example.common.GrpcServer;
import io.grpc.ClientInterceptor;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

import java.util.List;

/**
 * Interceptor has to be configured on the managed channel level
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractInterceptorTest {

    private GrpcServer grpcServer;
    protected ManagedChannel channel;
    protected BankServiceGrpc.BankServiceStub bankStub;
    protected BankServiceGrpc.BankServiceBlockingStub bankBlockingStub;

    protected abstract List<ClientInterceptor> getClientInterceptors();

    protected GrpcServer createServer() {
        return GrpcServer.create(6565, builder -> {
            builder.addService(new BankService())
                   .intercept(new GzipResponseInterceptor());
        });
    }

    @BeforeAll
    public void setup() {
        this.grpcServer = createServer();
        this.grpcServer.start();
        this.channel = ManagedChannelBuilder.forAddress("localhost", 6565)
                                            .usePlaintext()
                                            .intercept(getClientInterceptors())
                                            .build();
        this.bankStub = BankServiceGrpc.newStub(channel);
        this.bankBlockingStub = BankServiceGrpc.newBlockingStub(channel);
    }

    @AfterAll
    public void stop() {
        this.grpcServer.stop();
        this.channel.shutdownNow();
    }

}
