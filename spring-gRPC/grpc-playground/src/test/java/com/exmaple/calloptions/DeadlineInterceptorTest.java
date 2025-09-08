package com.exmaple.calloptions;

import com.example.calloptions.BalanceCheckRequest;
import com.example.calloptions.Money;
import com.example.calloptions.WithdrawRequest;
import com.exmaple.calloptions.clientinterceptor.DeadlineInterceptor;
import com.exmaple.common.ResponseObserver;
import io.grpc.ClientInterceptor;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;


public class DeadlineInterceptorTest extends AbstractInterceptorTest {

    /**
     * Config a global deadline interceptor
     */
    @Override
    protected List<ClientInterceptor> getClientInterceptors() {
        return List.of(new DeadlineInterceptor(Duration.ofSeconds(2)));
    }

    @Test
    public void defaultDeadlineTest(){
        var ex = Assertions.assertThrows(StatusRuntimeException.class, () -> {
            var request = BalanceCheckRequest.newBuilder()
                    .setAccountNumber(1)
                    .build();
            var response = this.bankBlockingStub.getAccountBalance(request);
        });
        Assertions.assertEquals(Status.Code.DEADLINE_EXCEEDED, ex.getStatus().getCode());
    }

    @Test
    public void overrideInterceptorTest(){
        var observer = ResponseObserver.<Money>create();
        var request = WithdrawRequest.newBuilder()
                                     .setAccountNumber(1)
                                     .setAmount(500)
                                     .build();
        this.bankStub
                // to override the global deadline config
//                .withDeadline(Deadline.after(6, TimeUnit.SECONDS))
                .withdraw(request, observer);
        observer.await();
    }

}
