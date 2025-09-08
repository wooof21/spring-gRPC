package com.exmaple.calloptions;

import com.example.calloptions.AccountBalance;
import com.example.calloptions.BalanceCheckRequest;
import com.example.calloptions.Money;
import com.example.calloptions.WithdrawRequest;
import com.example.calloptions10.BankService;
import com.example.calloptions10.Constants;
import com.example.calloptions10.serverinterceptor.UserTokenInterceptor;
import com.example.common.GrpcServer;
import com.exmaple.common.ResponseObserver;
import io.grpc.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

public class UserTokenInterceptorTest extends AbstractInterceptorTest {

    private static final Logger log = LoggerFactory.getLogger(UserTokenInterceptorTest.class);

    @Override
    protected List<ClientInterceptor> getClientInterceptors() {
        return Collections.emptyList();
    }

    @Override
    protected GrpcServer createServer() {
        return GrpcServer.create(6565, builder -> {
            builder.addService(new BankService())
                   .intercept(new UserTokenInterceptor());
        });
    }

    @Test
    public void unaryUserCredentialsTest() {
        List<AccountBalance> responses = new ArrayList<>();
        for (int i=3; i<=6; i++) {
            var request = BalanceCheckRequest.newBuilder()
                                             .setAccountNumber(i)
                                             .build();
            var response = this.bankBlockingStub
                    // pass the user credentials from client side
                    .withCallCredentials(new UserSessionToken("user-token-" + i))
                    .getAccountBalance(request);
            responses.add(response);
            log.info("{}", response);
        }

        Assertions.assertEquals(4, responses.size());
    }

    @Test
    public void unaryUserInvalidCredentialsTest() {
        var ex = Assertions.assertThrows(StatusRuntimeException.class, () -> {
            var request = BalanceCheckRequest.newBuilder()
                                             .setAccountNumber(1)
                                             .build();
            var response = this.bankBlockingStub
                    .withCallCredentials(new UserSessionToken("user-token-7"))
                    .getAccountBalance(request);
            log.info("{}", response);
        });
        Assertions.assertEquals(Status.Code.UNAUTHENTICATED, ex.getStatus().getCode());
    }


    @Test
    public void streamingUserCredentialsTest() {
        for (int i=3; i<=7; i++) {
            var observer = ResponseObserver.<Money>create();
            var request = WithdrawRequest.newBuilder()
                                         .setAccountNumber(i)
                                         .setAmount(100)
                                         .build();
            this.bankStub
                    .withCallCredentials(new UserSessionToken("user-token-" + i))
                    .withdraw(request, observer);
            observer.await();

            if(i <= 4) {
                Assertions.assertEquals(1, observer.getItems().size());
                Assertions.assertEquals(100, observer.getItems().get(0).getAmount());
            }
            if(i > 4 && i <=6) {
                Assertions.assertEquals(0, observer.getItems().size());
                Assertions.assertNotNull(observer.getError());
                Assertions.assertEquals(Status.Code.PERMISSION_DENIED,
                        ((StatusRuntimeException) observer.getError()).getStatus().getCode());
            }
            if(i == 7) {
                Assertions.assertEquals(0, observer.getItems().size());
                Assertions.assertNotNull(observer.getError());
                Assertions.assertEquals(Status.Code.UNAUTHENTICATED,
                        ((StatusRuntimeException) observer.getError()).getStatus().getCode());
            }
        }
    }

    @Test
    public void primaryUserRoleTest() {
        var request = BalanceCheckRequest.newBuilder()
                .setAccountNumber(3)
                .build();
        var response = this.bankBlockingStub
                // pass the user credentials from client side
                .withCallCredentials(new UserSessionToken("user-token-" + 3))
                .getAccountBalance(request);

        Assertions.assertEquals(3000, response.getBalance());
    }

    @Test
    public void standardUserRoleTest() {
        var request = BalanceCheckRequest.newBuilder()
                .setAccountNumber(5)
                .build();
        var response = this.bankBlockingStub
                // pass the user credentials from client side
                .withCallCredentials(new UserSessionToken("user-token-" + 5))
                .getAccountBalance(request);

        Assertions.assertEquals(4999, response.getBalance());
    }

    private static class UserSessionToken extends CallCredentials {

        private static final String TOKEN_FORMAT = "%s %s";
        private final String jwt;

        public UserSessionToken(String jwt) {
            this.jwt = jwt;
        }

        @Override
        public void applyRequestMetadata(RequestInfo requestInfo,
                                         Executor executor,
                                         MetadataApplier metadataApplier) {
            executor.execute(() -> {
                var metadata = new Metadata();
                metadata.put(Constants.USER_TOKEN_KEY, TOKEN_FORMAT.formatted(Constants.BEARER, jwt));
                metadataApplier.apply(metadata);
            });
        }

    }

}
