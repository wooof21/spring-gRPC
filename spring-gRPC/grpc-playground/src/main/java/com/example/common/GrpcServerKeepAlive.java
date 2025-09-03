package com.example.common;

import io.grpc.*;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Slf4j
public class GrpcServerKeepAlive {

    private final Server server;

    private GrpcServerKeepAlive(Server server){
        this.server = server;
    }

    // create a grpc server with default port 6565 and services
    public static GrpcServerKeepAlive create(BindableService... services){

        return create(6565, services);
    }

    public static GrpcServerKeepAlive create(int port, BindableService... services) {
        var builder = ServerBuilder.forPort(port)
                .keepAliveTime(20, TimeUnit.SECONDS)
                .keepAliveTimeout(5, TimeUnit.SECONDS)
                .maxConnectionIdle(25, TimeUnit.SECONDS);
        Arrays.asList(services).forEach(builder::addService);
        return new GrpcServerKeepAlive(builder.build());
    }

    public static GrpcServerKeepAlive create(int port, Consumer<NettyServerBuilder> consumer) {
        var builder = ServerBuilder.forPort(port);
        consumer.accept((NettyServerBuilder) builder);
        return new GrpcServerKeepAlive(builder.build());
    }

    // start the grpc server
    public GrpcServerKeepAlive start() {
        // get the list of services
        var services = server.getServices()
                .stream()
                .map(ServerServiceDefinition::getServiceDescriptor)
                .map(ServiceDescriptor::getName)
                .toList();
        try {
            server.start();
            log.info("GrpcServerKeepAlive started. listening on port {} - services: {}", server.getPort(), services);
            return this;
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public void await() {
        try{
            server.awaitTermination();
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        server.shutdownNow();
        log.info("Server stopped");
    }
}
