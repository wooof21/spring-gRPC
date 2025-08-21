package com.example.type03.composition;

import com.example.grpcplayground.models.types.composition.Address;
import com.example.grpcplayground.models.types.composition.School;
import com.example.grpcplayground.models.types.composition.Student;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {

    public static void main(String[] args) {

        var address = Address.newBuilder()
                .setStreet("123 Main St")
                .setCity("Springfield")
                .setState("IL")
                .build();
        var student = Student.newBuilder()
                .setName("john")
                .setAddress(address)
                .build();
        var school = School.newBuilder()
                .setId(1)
                .setName("Springfield High")
                .setAddress(address.toBuilder().setStreet("456 Elm St").build())
                .build();

        log.info("Student: {}", student);
        log.info("School: {}", school);
    }
}
