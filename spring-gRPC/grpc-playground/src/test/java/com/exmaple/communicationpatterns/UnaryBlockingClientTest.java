package com.exmaple.communicationpatterns;

import com.example.communicationpatterns.BalanceCheckRequest;
import com.google.protobuf.Empty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnaryBlockingClientTest extends AbstractTest {

    private static final Logger log = LoggerFactory.getLogger(UnaryBlockingClientTest.class);

    @Test
    public void getBalanceTest(){
        var request = BalanceCheckRequest.newBuilder()
                .setAccountNumber(2)
                .build();
        var balance = this.bankBlockingStub.getAccountBalance(request);
        log.info("Unary balance received: {}", balance);
        Assertions.assertEquals(2000, balance.getBalance());
    }

    @Test
    public void allAccountsTest(){
        var allAccounts = this.bankBlockingStub.getAllAccounts(Empty.getDefaultInstance());
        log.info("All accounts size: {}", allAccounts.getAccountsCount());
        Assertions.assertEquals(9, allAccounts.getAccountsCount());
    }

}
