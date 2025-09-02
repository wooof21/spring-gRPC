package com.exmaple.trailermetadata;

import com.example.validationanderrorhandling.trailermetadata.AccountBalance;
import com.example.validationanderrorhandling.trailermetadata.BalanceCheckRequest;
import com.example.validationanderrorhandling.trailermetadata.ErrorMessage;
import com.example.validationanderrorhandling.trailermetadata.ValidationCode;
import com.exmaple.common.ResponseObserver;
import io.grpc.StatusRuntimeException;
import io.grpc.protobuf.ProtoUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UnaryInputValidationTest extends AbstractTest {

    @Test
    public void blockingInputValidationTest(){
        var ex = Assertions.assertThrows(StatusRuntimeException.class, () -> {
            var request = BalanceCheckRequest.newBuilder()
                                             .setAccountNumber(11)
                                             .build();
            var response = this.bankBlockingStub.getAccountBalance(request);
        });
        Assertions.assertEquals(ValidationCode.INVALID_ACCOUNT, getValidationCode(ex));
    }

    @Test
    public void asyncInputValidationTest(){
        var request = BalanceCheckRequest.newBuilder()
                                         .setAccountNumber(11)
                                         .build();
        var observer = ResponseObserver.<AccountBalance>create();
        this.bankStub.getAccountBalance(request, observer);
        observer.await();

        Assertions.assertTrue(observer.getItems().isEmpty());
        Assertions.assertNotNull(observer.getError());
        Assertions.assertEquals(ValidationCode.INVALID_ACCOUNT,
                getValidationCode(observer.getError()));
    }

}
