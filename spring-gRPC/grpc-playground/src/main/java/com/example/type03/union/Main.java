package com.example.type03.union;

import com.example.grpcplayground.models.types.union.Credentials;
import com.example.grpcplayground.models.types.union.Email;
import com.example.grpcplayground.models.types.union.Phone;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {

    public static void main(String[] args) {
        var email = Email.newBuilder()
                .setAddress("john@gmai.com")
                .setProvider("gmail")
                .build();

        var phone = Phone.newBuilder()
                .setNumber(123456789L)
                .setAreaCode(1)
                .build();

        var cred1 = Credentials.newBuilder()
                .setEmail(email)
                .build();
        login(cred1);

        var cred2 = Credentials.newBuilder()
                .setPhone(phone)
                .build();
        login(cred2);

        var cred3 = Credentials.newBuilder().build();
        login(cred3);

        // when set both, the last one wins
        var cred4 = Credentials.newBuilder()
                .setEmail(email)
                .setPhone(phone)
                .build();
        login(cred4);
    }

    private static void login(Credentials cred) {
        switch (cred.getLoginTypeCase()) {
            case LOGINTYPE_NOT_SET -> log.info("No login type set");
            case EMAIL -> {
                var email = cred.getEmail();
                log.info("Logging in with email: {} from provider: {}", email.getAddress(), email.getProvider());
            }
            case PHONE -> {
                var phone = cred.getPhone();
                log.info("Logging in with phone number: {} with area code: {}", phone.getNumber(), phone.getAreaCode());
            }
        }
    }
}
