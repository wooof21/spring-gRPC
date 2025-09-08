package com.exmaple.ssl_tls;

import com.example.common.GrpcServer;
import com.example.ssl_tls11.BankService;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractTest {

    private static final Path KEY_STORE = Path.of("src/test/resources/certs/grpc.keystore.jks");
    private static final Path TRUST_STORE = Path.of("src/test/resources/certs/grpc.truststore.jks");
    private static final char[] PASSWORD = "changeit".toCharArray();

    private final GrpcServer grpcServer = GrpcServer.create(6565, b -> {
        b.addService(new BankService())
                /**
                 * When `.directExecutor()`: Better when no I/O or network calls
                 *  - This tells gRPC to execute calls directly in the transport thread.
                 *  - This can improve performance by reducing context switching and thread management overhead.
                 *  - However, it can also lead to blocking the transport thread if the service implementation
                 *    performs long-running or blocking operations, which can degrade overall server performance.
                 * When `.executor(Executors.newVirtualThreadPerTaskExecutor())`:
                 *  - This configures gRPC to use a virtual thread per task executor for handling incoming requests.
                 *  - Virtual threads are lightweight threads that are managed by the Java Virtual Machine (JVM)
                 *    rather than the operating system.
                 *  - This allows for a large number of concurrent threads with minimal resource overhead,
                 *    making it suitable for I/O-bound operations typical in gRPC services.
                 *  - Using virtual threads can improve scalability and responsiveness of the server,
                 *    especially under high load, as it can handle many simultaneous requests without
                 *    the limitations imposed by traditional OS threads.
                 */
//                .directExecutor()
                .executor(Executors.newVirtualThreadPerTaskExecutor())
                // enable TLS
                .sslContext(serverSslContext());
    });

    @BeforeAll
    public void start() {
        this.grpcServer.start();
    }

    @AfterAll
    public void stop() {
        this.grpcServer.stop();
    }

    /**
     * SSL context for server
     *  - Configures server-side SSL using a KeyManagerFactory
     *      (which loads the private key + certificate from keystore).
     *  - Wraps it with gRPC-specific settings (GrpcSslContexts).
     */
    protected SslContext serverSslContext() {
        return handleException(() ->
                GrpcSslContexts.configure(SslContextBuilder
                        .forServer(getKeyManagerFactory()))
                        .build()
        );
    }

    /**
     * SSL context for client
     *  - Configures client-side SSL using a TrustManagerFactory
     *      (which loads the trusted certificates from truststore).
     *  - Uses a TrustManagerFactory to validate server certificates against the truststore.
     */
    protected SslContext clientSslContext() {
        return handleException(() ->
                GrpcSslContexts.configure(SslContextBuilder
                        .forClient().trustManager(getTrustManagerFactory()))
                        .build()
        );
    }

    // load server’s private key + certificate (to authenticate itself).
    protected KeyManagerFactory getKeyManagerFactory() {
        return handleException(() -> {
            var kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            var keyStore = KeyStore.getInstance(KEY_STORE.toFile(), PASSWORD);
            kmf.init(keyStore, PASSWORD);
            return kmf;
        });
    }

    // loads CA certificates (to validate peers).
    protected TrustManagerFactory getTrustManagerFactory() {
        return handleException(() -> {
            var tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            var trustStore = KeyStore.getInstance(TRUST_STORE.toFile(), PASSWORD);
            tmf.init(trustStore);
            return tmf;
        });
    }

    // generic exception handler to wrap checked exceptions in runtime exceptions
    private <T> T handleException(Callable<T> callable) {
        try {
            return callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
