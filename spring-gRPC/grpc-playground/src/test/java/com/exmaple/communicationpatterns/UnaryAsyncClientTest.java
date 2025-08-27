package com.exmaple.communicationpatterns;

import com.example.communicationpatterns.AccountBalance;
import com.example.communicationpatterns.AllAccountsResponse;
import com.example.communicationpatterns.BalanceCheckRequest;
import com.exmaple.common.ResponseObserver;
import com.google.protobuf.Empty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnaryAsyncClientTest extends AbstractTest {

    private static final Logger log = LoggerFactory.getLogger(UnaryAsyncClientTest.class);

    @Test
    public void getBalanceTest() {
        var request = BalanceCheckRequest.newBuilder().setAccountNumber(1).build();
        var observer = ResponseObserver.<AccountBalance>create();
        this.bankStub.getAccountBalance(request, observer);
        observer.await();
        Assertions.assertEquals(1, observer.getItems().size());
        Assertions.assertEquals(1000, observer.getItems().getFirst().getBalance());
        Assertions.assertNull(observer.getError());
    }

    @Test
    public void allAccountsTest(){
        var observer = ResponseObserver.<AllAccountsResponse>create();
        this.bankStub.getAllAccounts(Empty.getDefaultInstance(), observer);
        observer.await();
        Assertions.assertEquals(1, observer.getItems().size());
        Assertions.assertEquals(9, observer.getItems().getFirst().getAccountsCount());
        Assertions.assertNull(observer.getError());
    }

}
