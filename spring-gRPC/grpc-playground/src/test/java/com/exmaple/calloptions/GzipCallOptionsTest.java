package com.exmaple.calloptions;

import com.example.calloptions.BalanceCheckRequest;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GzipCallOptionsTest extends AbstractTest {

    private static final Logger log = LoggerFactory.getLogger(GzipCallOptionsTest.class);

    @Test
    public void gzipTest() {
        var request = BalanceCheckRequest.newBuilder()
                                         .setAccountNumber(1)
                                         .build();
        /**
         * Client:
         * OUTBOUND HEADERS: streamId=3 headers=GrpcHttp2OutboundHeaders[:authority: localhost:6565,
         * :path: /calloptions.BankService/GetAccountBalance, :method: POST, :scheme: http,
         * content-type: application/grpc, te: trailers, user-agent: grpc-java-netty/1.72.0,
         * grpc-accept-encoding: gzip] streamDependency=0 weight=16 exclusive=false padding=0
         * endStream=false
         *
         * no grpc-encoding header
         *
         * Server:
         * INBOUND HEADERS: streamId=3 headers=GrpcHttp2ResponseHeaders[:status: 200,
         * content-type: application/grpc, grpc-encoding: identity, grpc-accept-encoding: gzip]
         * padding=0 endStream=false
         *
         * grpc-encoding: identity -> message was not compressed
         *
         * after enabling gzip ->
         * Client header has: grpc-encoding: gzip
         * Server header still: grpc-encoding: identity - since server was not enabled
         *
         * To enable gzip on server side - enable it in BankService class
         */

        var response = this.bankBlockingStub
                .withCompression("gzip")
                .getAccountBalance(request);

        log.debug("Request: {}", response);
    }

}
