package com.trading.user.integration;

import com.trading.common.Stock;
import com.trading.trader.StockTradeRequest;
import com.trading.trader.TradeAction;
import com.trading.trader.TraderInformationRequest;
import com.trading.trader.TraderServiceGrpc;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// overwrite the gRPC server config properties for testing
// using the gRPC in-process server for testing
@SpringBootTest(properties = {
        "grpc.server.port=-1",
        "grpc.server.in-process-name=integration-test",
        "grpc.client.trader-service.address=in-process:integration-test"
})
public class TraderServiceTest {

    @GrpcClient("trader-service")
    private TraderServiceGrpc.TraderServiceBlockingStub stub;

    @Test
    public void userInformationTest() {
        var request = TraderInformationRequest.newBuilder()
                                            .setTraderId(1)
                                            .build();
        var response = this.stub.getTraderInformation(request);
        Assertions.assertEquals(10_000, response.getBalance());
        Assertions.assertEquals("John", response.getName());
        Assertions.assertTrue(response.getHoldingsList().isEmpty());
    }

    @Test
    public void unknownUserTest() {
        var ex = Assertions.assertThrows(StatusRuntimeException.class, () -> {
            var request = TraderInformationRequest.newBuilder()
                                                .setTraderId(10)
                                                .build();
            var response = this.stub.getTraderInformation(request);
        });
        Assertions.assertEquals(Status.Code.NOT_FOUND, ex.getStatus().getCode());
    }

    @Test
    public void unknownTickerBuyTest() {
        var ex = Assertions.assertThrows(StatusRuntimeException.class, () -> {
            var request = StockTradeRequest.newBuilder()
                                           .setTraderId(1)
                                           .setPrice(1)
                                           .setQuantity(1)
                                           .setAction(TradeAction.BUY)
                                           .build();
            this.stub.tradeStock(request);
        });
        Assertions.assertEquals(Status.Code.INVALID_ARGUMENT, ex.getStatus().getCode());
    }

    @Test
    public void insufficientSharesTest() {
        var ex = Assertions.assertThrows(StatusRuntimeException.class, () -> {
            var request = StockTradeRequest.newBuilder()
                                           .setTraderId(1)
                                           .setPrice(1)
                                           .setQuantity(1000)
                                           .setStock(Stock.APPLE)
                                           .setAction(TradeAction.SELL)
                                           .build();
            this.stub.tradeStock(request);
        });
        Assertions.assertEquals(Status.Code.FAILED_PRECONDITION, ex.getStatus().getCode());
    }

    @Test
    public void insufficientBalanceTest() {
        var ex = Assertions.assertThrows(StatusRuntimeException.class, () -> {
            var request = StockTradeRequest.newBuilder()
                                           .setTraderId(1)
                                           .setPrice(1)
                                           .setQuantity(10001)
                                           .setStock(Stock.APPLE)
                                           .setAction(TradeAction.BUY)
                                           .build();
            this.stub.tradeStock(request);
        });
        Assertions.assertEquals(Status.Code.FAILED_PRECONDITION, ex.getStatus().getCode());
    }

    @Test
    public void buySellTest() {
        // buy
        var buyRequest = StockTradeRequest.newBuilder()
                                       .setTraderId(2)
                                       .setPrice(100)
                                       .setQuantity(5)
                                       .setStock(Stock.APPLE)
                                       .setAction(TradeAction.BUY)
                                       .build();
        var buyResponse = this.stub.tradeStock(buyRequest);

        // check holding
        var userRequest = TraderInformationRequest.newBuilder().setTraderId(2).build();
        var userResponse = this.stub.getTraderInformation(userRequest);
        Assertions.assertEquals(1, userResponse.getHoldingsCount());
        Assertions.assertEquals(Stock.APPLE, userResponse.getHoldingsList().getFirst().getStock());

        // validate balance
        Assertions.assertEquals(9500, userResponse.getBalance());

        // sell
        var sellRequest = buyRequest.toBuilder().setAction(TradeAction.SELL).setPrice(102).build();
        var sellResponse = this.stub.tradeStock(sellRequest);

        // validate balance
        Assertions.assertEquals(10010, sellResponse.getBalance());
    }

}
