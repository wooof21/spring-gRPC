package com.example.type03.singleobject;

import com.example.grpcplayground.models.types.singleobject.PersonSingle;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PerformanceTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {

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

        /**
         *   `proto` only encoding values that are set,
         *   instead it uses the field number(1, 2, 3...) to encode
         *   when no values are set:
         *      - 0 for numbers
         *      - false for booleans
         *      - empty string ""
         *      - not set at all
         *   it does not encode them - treat as default values
         *
         *   `json` encodes all values with variable names - take more space/bytes to encode
         */
        log.info("proto bytes length: {}", proto(person).length);
        log.info("json bytes length: {}", json(person).length);

        for (int i=0; i<5; i++) {
            performanceTest("ProtoBuf", () -> proto(person));
            performanceTest("JSON", () -> json(person));
        }
    }

    private static byte[] proto(PersonSingle person) {
        try {
            var bytes = person.toByteArray();
            PersonSingle.parseFrom(bytes);
            return bytes;
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] json(PersonSingle person) {
        try {
            PersonJSON personJSON = PersonJSON.builder()
                    .fullName(person.getFullName())
                    .age(person.getAge())
                    .email(person.getEmail())
                    .employed(person.getEmployed())
                    .salary(person.getSalary())
                    .bankAccountNumber(person.getBankAccountNumber())
                    .balance(person.getBalance())
                    .hobbies(
                            person.getHobbiesList().toArray(new String[0])
                    )
                    .attributes(
                            person.getAttributesMap()
                    )
                    .phoneNumber(person.hasPhoneNumber() ? person.getPhoneNumber() : null)
                    .alternateEmail(person.hasAlternateEmail() ? person.getAlternateEmail() : null)
                    .build();

            byte[] json = objectMapper.writeValueAsBytes(personJSON);
            objectMapper.readValue(json, PersonJSON.class);
            return json;
        } catch (Exception e) {
            throw new RuntimeException("Error processing JSON", e);
        }
    }

    private static void performanceTest(String testName, Runnable runnable) {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1_000_000; i++) {
            runnable.run();
        }
        long end = System.currentTimeMillis();
        long duration = end - start;
        log.info("Test - [{}] - took {} ms for 1_000_000 iterations", testName, duration);
    }
}
