package com.example.calloptions10;

import io.grpc.Context;
import io.grpc.Metadata;

import java.util.Set;

public class Constants {

    public static final Metadata.Key<String> API_KEY =
            Metadata.Key.of("api-key", Metadata.ASCII_STRING_MARSHALLER);


    // client header: Authorization: Bearer eyJhbGciOiAiSFMyNTYiLCAidHlwIjogIkpXVCJ1234567890
    public static final Metadata.Key<String> USER_TOKEN_KEY =
            Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);
    public static final String BEARER = "Bearer";


    public static final Context.Key<UserRole> USER_ROLE_KEY =
            Context.key("user-role");

    public static final String API_KEY_INTERCEPTOR_APPLY_METHOD =
            "calloptions.BankService/GetAccountBalance";


    public static final Set<String> PRIME_SET = Set.of("user-token-3", "user-token-4");

    public static final Set<String> STANDARD_SET = Set.of("user-token-5", "user-token-6");

}
