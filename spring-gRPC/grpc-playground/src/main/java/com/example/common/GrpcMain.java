package com.example.common;

import com.example.communicationpatterns06.BankService;
import io.grpc.BindableService;
import io.grpc.ServerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;


@Slf4j
public class GrpcMain {

    public static void main(String[] args) {

        try (var context = new AnnotationConfigApplicationContext("com.example")) {
            var services = context.getBeansOfType(GrpcService.class).values();
            var interceptors = context.getBeansOfType(ServerInterceptor.class).values();

            services.forEach(service -> {
                log.info("Grpc services: {}", service.getClass().getSimpleName());
            });
            interceptors.forEach(interceptor -> {
                log.info("Grpc interceptors: {}", interceptor.getClass().getSimpleName());
            });

            GrpcServer.create(interceptors, services.toArray(new BindableService[0]))
                    .start()
                    .await();
        }
    }



    private static class BankInstance1 {
        public static void main(String[] args) {
            GrpcServer.create(6565, new BankService())
                    .start()
                    .await();
        }
    }

    private static class BankInstance2 {
        public static void main(String[] args) {
            GrpcServer.create(7575, new BankService())
                    .start()
                    .await();
        }
    }
}
