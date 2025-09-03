package com.exmaple.timeout;

import com.example.timeout.Money;
import com.example.timeout.WithdrawRequest;
import com.exmaple.common.ResponseObserver;
import io.grpc.Deadline;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

public class ServerStreamingDeadlineTest extends AbstractTest {

    @Test
    public void blockingDeadlineTest() {
        var ex = Assertions.assertThrows(StatusRuntimeException.class, () -> {
            var request = WithdrawRequest.newBuilder()
                                         .setAccountNumber(1)
                                         .setAmount(500)
                                         .build();
            var iterator = this.bankBlockingStub
                    // NOTE: in server streaming -> timeout/deadline is configured on whole streaming call
                    //     -> all streaming expect to only take 2 seconds
                    .withDeadline(Deadline.after(2, TimeUnit.SECONDS))
                    .withdraw(request);
            while (iterator.hasNext()) {
                iterator.next();
            }
        });
        Assertions.assertEquals(Status.Code.DEADLINE_EXCEEDED, ex.getStatus().getCode());
    }

    @Test
    public void asyncDeadlineTest() {
        var observer = ResponseObserver.<Money>create();
        var request = WithdrawRequest.newBuilder()
                                     .setAccountNumber(1)
                                     .setAmount(500)
                                     .build();
        this.bankStub
                .withDeadline(Deadline.after(2, TimeUnit.SECONDS))
                .withdraw(request, observer);
        observer.await();
        // only receive 2 items with Context condition check
        Assertions.assertEquals(2, observer.getItems().size());
        Assertions.assertEquals(Status.Code.DEADLINE_EXCEEDED,
                Status.fromThrowable(observer.getError()).getCode());
    }

}
