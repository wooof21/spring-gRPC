package com.example.imports04;

import com.example.grpcplayground.models.imports.External;
import com.example.grpcplayground.models.imports.Person;
import com.example.grpcplayground.models.imports.common.Address;
import com.example.grpcplayground.models.imports.common.Car;
import com.google.protobuf.Int32Value;
import com.google.protobuf.Timestamp;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;

@Slf4j
public class Main {

    public static void main(String[] args) {

        importsInternal();

        importsExternal();
    }

    private static void importsInternal() {
        var address = Address.newBuilder()
                .setStreet("123 Main St")
                .setCity("Springfield")
                .setState("IL")
                .build();
        var car = Car.newBuilder()
                .setMake("Toyota")
                .setModel("Camry")
                .setYear(2020)
                .build();

        var person = Person.newBuilder()
                .setName("John Doe")
                .setAge(30)
                .setAddress(address)
                .setCar(car)
                .build();

        log.info("Person: {}", person);
    }

    private static void importsExternal() {
        var external = External.newBuilder()
                .setLoginTime(Timestamp.newBuilder().setSeconds(Instant.now().getEpochSecond()).build())
                .build();

        // to check if the 0 value is default or setted
        log.info("Age value : {} - real age? : {}", external.getAge().getValue(), external.hasAge());

        external = external.toBuilder()
                .setAge(Int32Value.of(0)).build();
        log.info("Age value : {} - real age? : {}",  external.getAge().getValue(), external.hasAge());

        log.info("Timestamp: {}", external.getLoginTime().getSeconds());

    }
}
