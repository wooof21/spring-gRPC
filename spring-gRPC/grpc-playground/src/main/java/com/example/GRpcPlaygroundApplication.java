package com.example;

import com.example.grpcplayground.models.PersonOuterClass;
import com.example.grpcplayground.models.multifiles.Person;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GRpcPlaygroundApplication {
    public static void main(String[] args) {

        simplePersonDemo();
        sampleMultiFiles();
        compare();

    }

    private static void simplePersonDemo() {
        PersonOuterClass.Person sample = PersonOuterClass.Person.newBuilder()
                .setName("sam")
                .setAge(12)
                .build();
        log.info("Sample: {}", sample);
    }

    private static Person sampleMultiFiles() {
        Person sampleMultiFile =
                Person.newBuilder().setName("Sample Multi File").setAge(22).build();

        log.info("Sample Multi File: {}", sampleMultiFile);
        return sampleMultiFile;
    }

    private static void compare() {
        var p1 = sampleMultiFiles();
        var p2 = sampleMultiFiles();

        // value comparison: true -> both objects are same
        log.info("Equals: {}", p1.equals(p2));
        // memory address comparison: false -> 2 objects point to different memory locations
        log.info("== : {}", p1 == p2);

        // object is immutable, no setters available
        // p1.setAge(23); // this will not compile

        // to change the value, either create a new object
        // or use toBuilder() to create a new object with modified values
        // other fields will be copied from the original object
        // toBuilder() is available only for objects generated with the proto3 syntax
        var p3 = p1.toBuilder().setName("toBuilder() Change Name").build();
        log.info("p3: {}", p3);

        // cannot set a field to null, as proto3 does not support null values
        // var p4 = p1.toBuilder().setName(null).build(); // this will throw an exception
        // when do not want a field to be set, use clear method
        var p4 = p1.toBuilder().clearName().build();
        log.info("p4: {}", p4);

    }
}