package com.exmaple.trailermetadata;

import com.example.common.GrpcServer;
import com.example.trailermetadata08.BankService;
import com.example.validationanderrorhandling.trailermetadata.BankServiceGrpc;
import com.example.validationanderrorhandling.trailermetadata.ErrorMessage;
import com.example.validationanderrorhandling.trailermetadata.ValidationCode;
import com.exmaple.common.AbstractChannelTest;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.protobuf.ProtoUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import java.util.Optional;

public abstract class AbstractTest extends AbstractChannelTest {

    private static final Metadata.Key<ErrorMessage> METADATA_KEY =
            ProtoUtils.keyForProto(ErrorMessage.getDefaultInstance());

    private final GrpcServer grpcServer = GrpcServer.create(new BankService());
    protected BankServiceGrpc.BankServiceStub bankStub;
    protected BankServiceGrpc.BankServiceBlockingStub bankBlockingStub;

    @BeforeAll
    public void setup() {
        this.grpcServer.start();
        this.bankStub = BankServiceGrpc.newStub(channel);
        this.bankBlockingStub = BankServiceGrpc.newBlockingStub(channel);
    }

    @AfterAll
    public void stop() {
        this.grpcServer.stop();
    }

    protected ValidationCode getValidationCode(Throwable throwable) {
        return Optional.ofNullable(Status.trailersFromThrowable(throwable))
                       .map(m -> m.get(METADATA_KEY))
                       .map(ErrorMessage::getValidationCode)
                       .orElseThrow();
    }

}
