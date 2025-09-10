package com.trading.aggregator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trading.aggregator.mockservice.StockMockService;
import com.trading.aggregator.model.PriceUpdateModel;
import com.trading.common.Stock;
import net.devh.boot.grpc.server.service.GrpcService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.annotation.DirtiesContext;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * DirtiesContext:
 *  - when run all integration tests together, some tests may fail since the
 *      state was changed by other tests.
 *  - this annotation tells Spring to reload the context before this test class
 *      so that the state is reset.
 */
@DirtiesContext
@SpringBootTest(properties = {
        "grpc.server.port=-1",
        "grpc.server.in-process-name=integration-test",
        "grpc.client.stock-service.address=in-process:integration-test"
}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StockUpdatesTest {

    private static final Logger log = LoggerFactory.getLogger(StockUpdatesTest.class);
    private static final String STOCK_UPDATES_ENDPOINT = "http://localhost:%d/stock/updates";

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private TestRestTemplate restTemplate;

    @TestConfiguration
    static class TestConfig {

        @GrpcService
        public StockMockService stockMockService(){
            return new StockMockService();
        }

    }

    @Test
    public void stockUpdatesTest() {
        // execute: return a stream of data
        var list = this.restTemplate.execute(
                STOCK_UPDATES_ENDPOINT.formatted(port),
                HttpMethod.GET,
                null,
                this::getResponse
        );
        Assertions.assertEquals(5, list.size());
        Assertions.assertEquals(Stock.APPLE.toString(), list.getFirst().stock());
        Assertions.assertEquals(1, list.getFirst().price());
    }

    // convert raw input stream to list of PriceUpdateModel
    private List<PriceUpdateModel> getResponse(ClientHttpResponse clientHttpResponse) {
        var list = new ArrayList<PriceUpdateModel>();
        try(var reader = new BufferedReader(new InputStreamReader(clientHttpResponse.getBody()))) {
            String line;
            while(Objects.nonNull(line = reader.readLine())) {
                if(!line.isEmpty()) {
                    log.info("Test streaming: {}", line);
                    if("event:price-update".equals(line)) continue;
                    var dto = mapper.readValue(line.substring("data:".length()), PriceUpdateModel.class);
                    list.add(dto);
                }
            }
        } catch (Exception e) {
            log.error("streaming error: ", e);
        }
        return list;
    }

}
