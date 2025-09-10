package com.trading.user.config;

import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.serverfactory.GrpcServerConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;

@Configuration
@Slf4j
public class ServerConfiguration {

    @Bean
    public GrpcServerConfigurer serverConfig() {
        return serverBuilder ->
                serverBuilder
                        .executor(Executors.newVirtualThreadPerTaskExecutor())
//                        .intercept()
//                        .addService((BindableService) services)
                        ;
    }
}
