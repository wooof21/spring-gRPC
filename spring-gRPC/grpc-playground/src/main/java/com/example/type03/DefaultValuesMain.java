package com.example.type03;

import com.example.grpcplayground.models.types.collection.Library;
import com.example.grpcplayground.models.types.composition.Address;
import com.example.grpcplayground.models.types.composition.School;
import com.example.grpcplayground.models.types.map_enum.Car;
import com.example.grpcplayground.models.types.map_enum.Dealer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultValuesMain {

    public static void main(String[] args) {

        var school = School.newBuilder().build();

        // number -> default 0
        log.info("Default School ID: {}", school.getId());
        // string -> default empty ""
        log.info("Default School Name: {}", school.getName());
        // object -> default empty object
        log.info("Default School Address: {}", school.getAddress());
        // no NPE
        log.info("Access Empty Address Street: {}", school.getAddress().getStreet());

        log.info("Default? : {}", school.getAddress().equals(Address.getDefaultInstance()));

        // to check if school has address set
        log.info("Has Address? : {}", school.hasAddress());

        // collection -> default empty collection []
        var library = Library.newBuilder().build();
        log.info("Collection Library Default: {}", library.getBooksList());

        // map -> default empty map {}
        var dealer = Dealer.newBuilder().build();
        log.info("Map Dealer Default: {}", dealer.getInventoryMap());

        // enum -> default value is the first defined value in the enum
        // label 0 is reserved for the default value
        var car = Car.newBuilder().build();
        log.info("Enum Car Default: {}", car.getBodyStyle());

        // to check the number field if 0 is an actual value instead of default value
        // primitive types no "hasField" method
        // use google wrapper types -> in /imports04

        // new added in proto3: add "optional" keyword before the type
        // then the compiler will generate the "hasField" method

    }
}
