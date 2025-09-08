package com.exmaple.calloptions;

import com.example.calloptions.Money;
import com.example.calloptions.WithdrawRequest;
import com.exmaple.common.ResponseObserver;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;

public class ExecutorCallOptionsTest extends AbstractTest {

    private static final Logger log = LoggerFactory.getLogger(ExecutorCallOptionsTest.class);

    @Test
    public void executorTest() {
        var observer = ResponseObserver.<Money>create();
        var request = WithdrawRequest.newBuilder()
                                     .setAccountNumber(1)
                                     .setAmount(300)
                                     .build();
        this.bankStub
                // config the Executor for the async call
                // using JDK21 virtual threads
                .withExecutor(Executors.newVirtualThreadPerTaskExecutor())
                .withdraw(request, observer);
        observer.await();
    }

}
