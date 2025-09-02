package com.exmaple.validationanderrorhandling;

import com.example.validationanderrorhandling.Money;
import com.example.validationanderrorhandling.WithdrawRequest;
import com.exmaple.common.ResponseObserver;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class ServerStreamingInputValidationTest extends AbstractTest {

    @ParameterizedTest
    @MethodSource("testData")
    public void blockingInputValidationTest(WithdrawRequest request, Status.Code code) {
        var ex = Assertions.assertThrows(StatusRuntimeException.class, () -> {
            var response = this.bankBlockingStub.withdraw(request).hasNext();
        });
        Assertions.assertEquals(code, ex.getStatus().getCode());
    }

    @ParameterizedTest
    @MethodSource("testData")
    public void asyncInputValidationTest(WithdrawRequest request, Status.Code code) {
        var observer = ResponseObserver.<Money>create();
        this.bankStub.withdraw(request, observer);
        observer.await();

        Assertions.assertTrue(observer.getItems().isEmpty());
        Assertions.assertNotNull(observer.getError());
        Assertions.assertEquals(code,
                ((StatusRuntimeException) observer.getError()).getStatus().getCode());
    }

    private Stream<Arguments> testData() {
        return Stream.of(
                Arguments.of(WithdrawRequest.newBuilder()
                        .setAccountNumber(11).setAmount(100).build(), Status.Code.INVALID_ARGUMENT),
                Arguments.of(WithdrawRequest.newBuilder()
                        .setAccountNumber(1).setAmount(101).build(), Status.Code.INVALID_ARGUMENT),
                Arguments.of(WithdrawRequest.newBuilder()
                        .setAccountNumber(1).setAmount(1100).build(), Status.Code.FAILED_PRECONDITION)
        );
    }

}
