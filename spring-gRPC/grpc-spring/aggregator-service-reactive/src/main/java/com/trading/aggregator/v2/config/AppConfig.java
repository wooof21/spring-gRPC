package com.trading.aggregator.v2.config;

import com.trading.trader.v2.StockTradeRequest;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.channelfactory.GrpcChannelConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.config.WebFluxConfigurer;

import java.util.concurrent.Executors;

@Configuration
@Slf4j
public class AppConfig {

    @Bean
    public GrpcChannelConfigurer channelConfigurer() {
        return (channelBuilder, name) -> {
            log.info("Channel builder: {}", name);
//            channelBuilder.usePlaintext();
//            channelBuilder.intercept();
            channelBuilder.executor(Executors.newVirtualThreadPerTaskExecutor());
        };

    }
}
