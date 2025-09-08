package com.exmaple.calloptions;

import com.example.calloptions.BalanceCheckRequest;
import com.exmaple.calloptions.clientinterceptor.GzipRequestInterceptor;
import io.grpc.ClientInterceptor;
import org.junit.jupiter.api.Test;

import java.util.List;

public class GzipInterceptorTest extends AbstractInterceptorTest {

    @Override
    protected List<ClientInterceptor> getClientInterceptors() {
        // config a global client gzip interceptor
        return List.of(new GzipRequestInterceptor());
    }

    @Test
    public void gzipTest() {
        var request = BalanceCheckRequest.newBuilder()
                                         .setAccountNumber(1)
                                         .build();
        var response = this.bankBlockingStub.getAccountBalance(request);
    }

}
