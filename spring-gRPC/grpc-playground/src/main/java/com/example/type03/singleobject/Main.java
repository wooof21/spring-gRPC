package com.example.type03.singleobject;

import com.example.grpcplayground.models.types.singleobject.PersonSingle;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {
    public static void main(String[] args) {

        var person = PersonSingle.newBuilder()
                .setFullName("John Doe")
                .setAge(30)
                .setEmail("john@gmail.com")
                .setEmployed(true)
                .setSalary(12345.67)
                .setBankAccountNumber(1234567890L)
                .setBalance(-456)
                .addHobbies("Reading")
                .addHobbies("Traveling")
                .addHobbies("Cooking")
                .putAttributes("height", "180cm")
                .putAttributes("weight", "75kg")
                // will only set one of the below two fields
                .setPhoneNumber("123-456-7890")
                .setAlternateEmail("john_doe@gmail.com")
                .build();

        log.info("PersonSingle: {}", person);

        var p1 = person.toBuilder().setHobbies(0, "Writing")
                .putAttributes("weight", "80kg")
                .setPhoneNumber("123-456-7890")
                .build();

        log.info("PersonSingle Updated: {}", p1);
    }
}
