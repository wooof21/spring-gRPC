package com.example.type03.singleobject;

import com.example.grpcplayground.models.types.singleobject.PersonSingle;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class Serialization {

    private static final Path FILE_PATH =
            Path.of("./grpc-playground/src/main/resources/person_single.out");

    public static void main(String[] args) throws IOException {

        PersonSingle person = PersonSingle.newBuilder()
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

        serialize(person);

        var dp = deserialize();
        log.info("Equals: {}", person.equals(dp));
        log.info("== : {}", person == dp);

        var bytes = person.toByteArray();
        log.info("bytes: {}", bytes);
        log.info("bytes read: {}", PersonSingle.parseFrom(bytes));
    }

    private static void serialize(PersonSingle person) throws IOException {
        //try-with resources to ensure the stream is closed automatically
        try(var outputStream = Files.newOutputStream(FILE_PATH)) {
            person.writeTo(outputStream);
        }
//        person.writeTo(Files.newOutputStream(FILE_PATH));
    }

    private static PersonSingle deserialize() throws IOException {
        try(var inputStream = Files.newInputStream(FILE_PATH)) {
            PersonSingle personSingle = PersonSingle.parseFrom(inputStream);
            log.info("Deserialized PersonSingle: {}", personSingle);
            return PersonSingle.parseFrom(inputStream);
        }
    }
}
