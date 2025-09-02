package com.exmaple.validationanderrorhandling;

import com.example.validationanderrorhandling.AccountBalance;
import com.example.validationanderrorhandling.BalanceCheckRequest;
import com.exmaple.common.ResponseObserver;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UnaryInputValidationTest extends AbstractTest {

    @Test
    public void blockingInputValidationTest() {
        var ex = Assertions.assertThrows(StatusRuntimeException.class, () -> {
            var request = BalanceCheckRequest.newBuilder()
                             .setAccountNumber(11)
                             .build();
            var response = this.bankBlockingStub.getAccountBalance(request);
        });
        Assertions.assertEquals(Status.Code.INVALID_ARGUMENT, ex.getStatus().getCode());
    }

    @Test
    public void asyncInputValidationTest() {
        var request = BalanceCheckRequest.newBuilder()
                                         .setAccountNumber(11)
                                         .build();
        var observer = ResponseObserver.<AccountBalance>create();
        this.bankStub.getAccountBalance(request, observer);
        observer.await();

        Assertions.assertTrue(observer.getItems().isEmpty());
        Assertions.assertNotNull(observer.getError());
        Assertions.assertEquals(Status.Code.INVALID_ARGUMENT,
                ((StatusRuntimeException) observer.getError()).getStatus().getCode());
    }

}
