package com.exmaple.calloptions;

import com.example.calloptions.BalanceCheckRequest;
import com.example.calloptions10.BankService;
import com.example.calloptions10.Constants;
import com.example.calloptions10.serverinterceptor.ApiKeyValidationInterceptor;
import com.example.common.GrpcServer;
import io.grpc.ClientInterceptor;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.MetadataUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ClientApiKeyInterceptorTest extends AbstractInterceptorTest {

    private static final Logger log = LoggerFactory.getLogger(ClientApiKeyInterceptorTest.class);

    @Override
    protected List<ClientInterceptor> getClientInterceptors() {
        return List.of(
                MetadataUtils.newAttachHeadersInterceptor(getApiKey())
        );
    }

    // register the server interceptor - api key validation
    @Override
    protected GrpcServer createServer() {
        return GrpcServer.create(6565, builder -> {
            builder.addService(new BankService())
                   .intercept(new ApiKeyValidationInterceptor());
        });
    }

    @Test
    public void clientApiKeyTest() {
        var ex = Assertions.assertThrows(StatusRuntimeException.class, () -> {
            var request = BalanceCheckRequest.newBuilder()
                    .setAccountNumber(1)
                    .build();
            var response = this.bankBlockingStub.getAccountBalance(request);
            log.info("{}", response);
        });
        Assertions.assertEquals(Status.Code.UNAUTHENTICATED, ex.getStatus().getCode());
    }

    private Metadata getApiKey() {
       var metadata = new Metadata();
       metadata.put(Constants.API_KEY, "bank-client-secret-invalid");
       return metadata;
    }

}
