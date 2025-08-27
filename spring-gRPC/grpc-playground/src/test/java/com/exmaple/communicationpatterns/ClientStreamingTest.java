package com.exmaple.communicationpatterns;

import com.example.communicationpatterns.AccountBalance;
import com.example.communicationpatterns.DepositRequest;
import com.example.communicationpatterns.Money;
import com.exmaple.common.ResponseObserver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

public class ClientStreamingTest extends AbstractTest {

    // blockingStub does not support client streaming
    @Test
    public void asyncDepositTest() {
        var responseObserver = ResponseObserver.<AccountBalance>create();
        var requestObserver = this.bankStub.deposit(responseObserver);

        // initial message - account number
        requestObserver.onNext(DepositRequest.newBuilder().setAccountNumber(5).build());

        // sending stream of money
        IntStream.rangeClosed(1, 10)
                .mapToObj(i -> Money.newBuilder().setAmount(100).build())
                .map(m -> DepositRequest.newBuilder().setMoney(m).build())
                .forEach(requestObserver::onNext);

       // notifying the server that deposit are done
        requestObserver.onCompleted();

        // at this point, response observer should receive a response
        responseObserver.await();

        // assert
        Assertions.assertEquals(1, responseObserver.getItems().size());
        Assertions.assertEquals(6000, responseObserver.getItems().getFirst().getBalance());
        Assertions.assertNull(responseObserver.getError());
    }

    @Test
    public void cancelTest() {
        var responseObserver = ResponseObserver.<AccountBalance>create();
        var requestObserver = this.bankStub.deposit(responseObserver);

        // initial message - account number
        requestObserver.onNext(DepositRequest.newBuilder().setAccountNumber(5).build());

        // sending stream of money
        IntStream.rangeClosed(1, 10)
                .mapToObj(i -> Money.newBuilder().setAmount(100).build())
                .map(m -> DepositRequest.newBuilder().setMoney(m).build())
                .forEach(requestObserver::onNext);


        requestObserver.onError(new RuntimeException("Cancelling deposit"));
        responseObserver.await();

        // assert
        Assertions.assertNotNull(responseObserver.getError());
    }

}
