package com.example.type03.singleobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(builderClassName = "PersonJSONBuilder")
public class PersonJSON {
    private String fullName;
    private int age;
    private String email;
    private boolean employed;
    private double salary;
    private long bankAccountNumber;
    private int balance;
    private String[] hobbies;
    private java.util.Map<String, String> attributes;
    private String phoneNumber;
    private String alternateEmail;

    public static class PersonJSONBuilder {
        public PersonJSON build() {
            if (phoneNumber != null && alternateEmail != null) {
                throw new IllegalArgumentException("Only one of phoneNumber or alternateEmail can be set.");
            }
            return new PersonJSON(fullName, age, email, employed, salary, bankAccountNumber, balance, hobbies, attributes, phoneNumber, alternateEmail);
        }
    }
}
