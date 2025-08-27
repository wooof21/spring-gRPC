package com.example.common;

import io.grpc.BindableService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;


@Slf4j
public class GrpcMain {

    public static void main(String[] args) {

        try (var context = new AnnotationConfigApplicationContext("com.example")) {
            var services = context.getBeansOfType(GrpcService.class).values();

            services.forEach(service -> {
                log.info("Grpc services: {}", service.getClass().getSimpleName());
            });

            GrpcServer.create(services.toArray(new BindableService[0]))
                    .start()
                    .await();
        }
    }
}
