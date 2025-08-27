package com.exmaple.communicationpatterns;

import com.example.communicationpatterns.TransferRequest;
import com.example.communicationpatterns.TransferResponse;
import com.example.communicationpatterns.TransferStatus;
import com.exmaple.common.ResponseObserver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class BiDirectionalStreamingTest extends AbstractTest {

    // bidirectional streaming test
    @Test
    public void transferTest() {
        var responseObserver = ResponseObserver.<TransferResponse>create();
        var requestObserver = this.transferStub.transfer(responseObserver);
        var requests = List.of(
                TransferRequest.newBuilder().setAmount(100)
                        .setFromAccount(6).setToAccount(6).build(),
                TransferRequest.newBuilder().setAmount(7000)
                        .setFromAccount(6).setToAccount(7).build(),
                TransferRequest.newBuilder().setAmount(100)
                        .setFromAccount(6).setToAccount(7).build(),
                TransferRequest.newBuilder().setAmount(100)
                        .setFromAccount(7).setToAccount(6).build()
        );
        requests.forEach(requestObserver::onNext);
        requestObserver.onCompleted();
        responseObserver.await();

        Assertions.assertEquals(4, responseObserver.getItems().size());
        this.validate(responseObserver.getItems().get(0),
                TransferStatus.REJECTED, 6000, 6000);
        this.validate(responseObserver.getItems().get(1),
                TransferStatus.REJECTED, 6000, 7000);
        this.validate(responseObserver.getItems().get(2),
                TransferStatus.COMPLETED, 5900, 7100);
        this.validate(responseObserver.getItems().get(3),
                TransferStatus.COMPLETED, 7000, 6000);

    }

    private void validate(TransferResponse response, TransferStatus status,
                          int fromAccountBalance, int toAccountBalance){
        Assertions.assertEquals(status, response.getStatus());
        Assertions.assertEquals(fromAccountBalance, response.getFromAccount().getBalance());
        Assertions.assertEquals(toAccountBalance, response.getToAccount().getBalance());
    }

}
