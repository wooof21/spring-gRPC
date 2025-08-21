package com.example.type03.map_enum;

import com.example.grpcplayground.models.types.map_enum.BodyStyle;
import com.example.grpcplayground.models.types.map_enum.Car;
import com.example.grpcplayground.models.types.map_enum.Dealer;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class Main {

    public static void main(String[] args) {
        var car1 = Car.newBuilder()
                .setMake("Toyota")
                .setModel("Camry")
                .setYear(2020)
                .setBodyStyle(BodyStyle.SEDAN)
                .build();

        var car2 = Car.newBuilder()
                .setMake("Ford")
                .setModel("F-150")
                .setYear(2021)
                .setBodyStyle(BodyStyle.TRUCK)
                .build();

        Map<Integer, Car> carInventory = Map.of(
                car1.getYear(), car1,
                car2.getYear(), car2
        );

        var dealer = Dealer.newBuilder()
                .putAllInventory(carInventory)
//                .putInventory(car1.getYear(), car1)
//                .putInventory(car2.getYear(), car2)
                .build();

        log.info("Dealer Inventory: {}", dealer.getInventoryMap());
        log.info("Car 2020: {}", dealer.getInventoryMap().get(2020));
        log.info("Contains 2021 Car: {}", dealer.getInventoryMap().containsKey(2021));
    }
}
