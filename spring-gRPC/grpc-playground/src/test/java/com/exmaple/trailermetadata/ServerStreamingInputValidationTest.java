package com.exmaple.trailermetadata;



import com.example.validationanderrorhandling.trailermetadata.Money;
import com.example.validationanderrorhandling.trailermetadata.ValidationCode;
import com.example.validationanderrorhandling.trailermetadata.WithdrawRequest;
import com.exmaple.common.ResponseObserver;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class ServerStreamingInputValidationTest extends AbstractTest {

    @ParameterizedTest
    @MethodSource("testData")
    public void blockingInputValidationTest(WithdrawRequest request, ValidationCode code){
        var ex = Assertions.assertThrows(StatusRuntimeException.class, () -> {
            var response = this.bankBlockingStub.withdraw(request).hasNext();
        });
        Assertions.assertEquals(code, getValidationCode(ex));
    }

    @ParameterizedTest
    @MethodSource("testData")
    public void asyncInputValidationTest(WithdrawRequest request, ValidationCode code){
        var observer = ResponseObserver.<Money>create();
        this.bankStub.withdraw(request, observer);
        observer.await();

        Assertions.assertTrue(observer.getItems().isEmpty());
        Assertions.assertNotNull(observer.getError());
        Assertions.assertEquals(code, getValidationCode(observer.getError()));
    }

    private Stream<Arguments> testData() {
        return Stream.of(
                Arguments.of(WithdrawRequest.newBuilder()
                        .setAccountNumber(11).setAmount(100).build(), ValidationCode.INVALID_ACCOUNT),
                Arguments.of(WithdrawRequest.newBuilder()
                        .setAccountNumber(1).setAmount(101).build(), ValidationCode.INVALID_AMOUNT),
                Arguments.of(WithdrawRequest.newBuilder()
                        .setAccountNumber(1).setAmount(1100).build(), ValidationCode.INSUFFICIENT_BALANCE)
        );
    }

}
