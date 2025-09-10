package com.trading.aggregator;

import com.trading.aggregator.mockservice.StockMockService;
import com.trading.aggregator.mockservice.TraderMockService;
import com.trading.common.Stock;
import com.trading.trader.StockTradeRequest;
import com.trading.trader.StockTradeResponse;
import com.trading.trader.TradeAction;
import com.trading.trader.TraderInformation;
import net.devh.boot.grpc.server.service.GrpcService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;

@DirtiesContext
@SpringBootTest(properties = {
        "grpc.server.port=-1",
        "grpc.server.in-process-name=integration-test",
        "grpc.client.trader-service.address=in-process:integration-test",
        "grpc.client.stock-service.address=in-process:integration-test"
}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TradeTest {

    private static final String TRADER_INFORMATION_ENDPOINT = "http://localhost:%d/trader/%d";
    private static final String TRADE_ENDPOINT = "http://localhost:%d/trade";

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @TestConfiguration
    static class TestConfig {

        @GrpcService
        public StockMockService stockMockService(){
            return new StockMockService();
        }

        @GrpcService
        public TraderMockService traderMockService(){
            return new TraderMockService();
        }

    }

    @Test
    public void traderInformationTest() {
        var url = TRADER_INFORMATION_ENDPOINT.formatted(port, 1);
        var response = this.restTemplate.getForEntity(url, TraderInformation.class);
        Assertions.assertEquals(200, response.getStatusCode().value());
        var trader = response.getBody();
        Assertions.assertNotNull(trader);
        Assertions.assertEquals(1, trader.getTraderId());
        Assertions.assertEquals("integration-test", trader.getName());
        Assertions.assertEquals(100, trader.getBalance());
    }

    @Test
    public void unknownTraderTest() {
        var url = TRADER_INFORMATION_ENDPOINT.formatted(port, 2);
        var response = this.restTemplate.getForEntity(url, TraderInformation.class);
        Assertions.assertEquals(404, response.getStatusCode().value());
        var trader = response.getBody();
        Assertions.assertNull(trader);
    }

    @Test
    public void tradeTest() {
        var tradeRequest = StockTradeRequest.newBuilder()
                .setTraderId(1)
                .setPrice(10)
                .setStock(Stock.APPLE)
                .setAction(TradeAction.BUY)
                .setQuantity(2)
                .build();
        var url = TRADE_ENDPOINT.formatted(port);
        var response = this.restTemplate.postForEntity(url, tradeRequest, StockTradeResponse.class);
        Assertions.assertEquals(200, response.getStatusCode().value());
        var tradeResponse = response.getBody();
        Assertions.assertNotNull(tradeResponse);
        Assertions.assertEquals(Stock.APPLE, tradeResponse.getStock());
        Assertions.assertEquals(1, tradeResponse.getTraderId());
        Assertions.assertEquals(15, tradeResponse.getPrice());
        Assertions.assertEquals(1000, tradeResponse.getTotalPrice());
        Assertions.assertEquals(0, tradeResponse.getBalance());
    }

}
