package com.example.common;

import io.grpc.*;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
public class GrpcServer {

//    public static void main(String[] args) {
//
//        var server = ServerBuilder.forPort(6565)
//                .addService(new BankService())
//                .build();
//        try {
//            server.start();
//            log.info("Server started. listening on port {}", server.getPort());
//            server.awaitTermination();
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }

    private final Server server;

    private GrpcServer(Server server){
        this.server = server;
    }

    // create a grpc server with default port 6565 and services
    public static GrpcServer create(BindableService... services) {

        return create(6565, services);
    }

    // create a grpc server with both services and interceptors
    public static GrpcServer create(Collection<ServerInterceptor> interceptors,
                                    BindableService... services) {
        return create(6565, builder -> {
            Arrays.asList(services).forEach(builder::addService);
            interceptors.forEach(builder::intercept);
        });
    }

    public static GrpcServer create(int port, BindableService... services) {
        return create(port, builder -> {
            Arrays.asList(services).forEach(builder::addService);
        });
//        var builder = ServerBuilder.forPort(port);
//        Arrays.asList(services).forEach(builder::addService);
//        return new GrpcServer(builder.build());
    }

    public static GrpcServer create(int port, Consumer<NettyServerBuilder> consumer) {
        var builder = ServerBuilder.forPort(port);
        consumer.accept((NettyServerBuilder) builder);
        return new GrpcServer(builder.build());
    }

    // start the grpc server
    public GrpcServer start() {
        // get the list of services
        var services = server.getServices()
                .stream()
                .map(ServerServiceDefinition::getServiceDescriptor)
                .map(ServiceDescriptor::getName)
                .toList();
        try {
            server.start();
            log.info("Server started. listening on port {} - services: {}", server.getPort(), services);
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
