package com.exmaple.ssl_tls;

import com.example.ssl_tls.BalanceCheckRequest;
import com.example.ssl_tls.BankServiceGrpc;
import com.example.ssl_tls.Money;
import com.example.ssl_tls.WithdrawRequest;
import com.exmaple.common.ResponseObserver;
import io.grpc.ManagedChannelBuilder;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrpcSSLTest extends AbstractTest {

    private static final Logger log = LoggerFactory.getLogger(GrpcSSLTest.class);

    @Test
    public void clientWithSSLTest() {
        /**
         * When `.usePlaintext()`:
         *  - This tells to skip TLS/SSL and use a plain (unencrypted) TCP connection.
         *  - Normally, gRPC uses HTTP/2 over TLS (h2) by default.
         *  - If don’t call .usePlaintext(), gRPC expects a secure channel (SslContext must be provided).
         *  - `.usePlaintext()` switches it to insecure mode:
         *      * No TLS handshake
         *      * No certificate validation
         *      * No encryption
         *      * Data is sent in cleartext over the network
         * When comment out `.usePlaintext()`:
         *  - It would work in real world when config the SSL with Certificate Authority (CA)
         *  - But here, use the self-signed certificate, so need to config the SSL context
         *    with TrustManagerFactory (which loads the trusted certificates from truststore).
         *  - This enables TLS/SSL for the channel, ensuring secure communication with the server
         *    using encryption and certificate validation.
         */
//        var channel = ManagedChannelBuilder.forAddress("localhost", 6565)
////                                            .usePlaintext()
//                                            .build();
        var channel = NettyChannelBuilder.forAddress("localhost", 6565)
                                         .sslContext(clientSslContext())
                                         .build();
        var stub = BankServiceGrpc.newBlockingStub(channel);
        var request = BalanceCheckRequest.newBuilder()
                                         .setAccountNumber(1)
                                         .build();
        var response = stub.getAccountBalance(request);
        log.info("{}", response);
        channel.shutdownNow();
    }

    @Test
    public void streaming() {
        var channel = NettyChannelBuilder.forAddress("localhost", 6565)
                                         .sslContext(clientSslContext())
                                         .build();
        var stub = BankServiceGrpc.newStub(channel);
        var request = WithdrawRequest.newBuilder()
                .setAccountNumber(1)
                .setAmount(30)
                .build();
        var observer = ResponseObserver.<Money>create();
        stub.withdraw(request, observer);
        observer.await();

        channel.shutdownNow();
    }

}
