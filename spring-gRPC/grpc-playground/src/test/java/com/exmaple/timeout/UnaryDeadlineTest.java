package com.exmaple.timeout;

import com.example.timeout.AccountBalance;
import com.example.timeout.BalanceCheckRequest;
import com.exmaple.common.ResponseObserver;
import io.grpc.Deadline;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

public class UnaryDeadlineTest extends AbstractTest {

    @Test
    public void blockingDeadlineTest() {
        var ex = Assertions.assertThrows(StatusRuntimeException.class, () -> {
            var request = BalanceCheckRequest.newBuilder()
                                             .setAccountNumber(1)
                                             .build();
            var response = this.bankBlockingStub
                    .withDeadline(Deadline.after(2, TimeUnit.SECONDS))
                    .getAccountBalance(request);
        });
        Assertions.assertEquals(Status.Code.DEADLINE_EXCEEDED, ex.getStatus().getCode());
    }

    @Test
    public void asyncDeadlineTest() {
        var observer = ResponseObserver.<AccountBalance>create();
        var request = BalanceCheckRequest.newBuilder()
                                         .setAccountNumber(1)
                                         .build();
        this.bankStub
                .withDeadline(Deadline.after(2, TimeUnit.SECONDS))
                .getAccountBalance(request, observer);
        observer.await();

        Assertions.assertTrue(observer.getItems().isEmpty());
        Assertions.assertEquals(Status.Code.DEADLINE_EXCEEDED,
                Status.fromThrowable(observer.getError()).getCode());
    }

}
