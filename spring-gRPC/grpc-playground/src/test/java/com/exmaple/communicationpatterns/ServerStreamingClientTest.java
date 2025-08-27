package com.exmaple.communicationpatterns;

import com.example.communicationpatterns.Money;
import com.example.communicationpatterns.WithdrawRequest;
import com.exmaple.common.ResponseObserver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

public class ServerStreamingClientTest extends AbstractTest {

    private static final Logger log = LoggerFactory.getLogger(ServerStreamingClientTest.class);

    @Test
    public void blockingClientWithdrawTest() {
        var request = WithdrawRequest.newBuilder()
                .setAccountNumber(3)
                .setAmount(20)
                .build();
        Iterator<Money> iterator = this.bankBlockingStub.withdraw(request);
        int count = 0;
        while (iterator.hasNext()) {
            log.info("Withdraw money: {}", iterator.next());
            count++;
        }
        Assertions.assertEquals(2, count);
    }

    @Test
    public void asyncClientWithdrawTest() {
        var request = WithdrawRequest.newBuilder()
                                     .setAccountNumber(4)
                                     .setAmount(20)
                                     .build();
        var observer = ResponseObserver.<Money>create();
        this.bankStub.withdraw(request, observer);
        observer.await();
        Assertions.assertEquals(2, observer.getItems().size());
        Assertions.assertEquals(10, observer.getItems().getFirst().getAmount());
        Assertions.assertNull(observer.getError());
    }

}
