package com.example.trailermetadata08.validator;

import com.example.trailermetadata08.repository.AccountRepo;
import com.example.validationanderrorhandling.trailermetadata.ErrorMessage;
import com.example.validationanderrorhandling.trailermetadata.ValidationCode;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.protobuf.ProtoUtils;

import java.util.Optional;

public class RequestValidator {

    // create key for metadata -> Key<ErrorMessage>
    private static final Metadata.Key<ErrorMessage> METADATA_KEY =
            ProtoUtils.keyForProto(ErrorMessage.getDefaultInstance());

    public static Optional<StatusRuntimeException> validateAccount(int accountNumber) {
        //valid account
        if(accountNumber > 0 && accountNumber < 11) {
            return Optional.empty();
        }
        var metadata = metadata(ValidationCode.INVALID_ACCOUNT);
        //invalid with status and description
        return Optional.of(Status.INVALID_ARGUMENT
                .withDescription("Account number should be between 1 and 10")
                .asRuntimeException(metadata)
        );
    }

    public static Optional<StatusRuntimeException> isAccountExist(int accountNumber) {
        if(AccountRepo.isAccountExist(accountNumber)) {
            return Optional.empty();
        }
        var metadata = metadata(ValidationCode.ACCOUNT_NOT_FOUND);
        return Optional.of(Status.NOT_FOUND
                .withDescription("Account number - " + accountNumber + " - not found")
                .asRuntimeException(metadata)
        );
    }

    // amount divisible by 100
    public static Optional<StatusRuntimeException> isAmountValidate(int amount) {
        if(amount > 0 && amount % 100 == 0) {
            return Optional.empty();
        }
        var metadata = metadata(ValidationCode.INVALID_AMOUNT);
        return Optional.of(Status.INVALID_ARGUMENT
                .withDescription("Requested amount should be divisible by 100")
                .asRuntimeException(metadata)
        );
    }

    public static Optional<StatusRuntimeException> hasSufficientBalance(int amount, int balance) {
        if(amount <= balance) {
            return Optional.empty();
        }
        var metadata = metadata(ValidationCode.INSUFFICIENT_BALANCE);
        return Optional.of(Status.FAILED_PRECONDITION
                .withDescription("Insufficient balance")
                .asRuntimeException(metadata)
        );
    }

    private static Metadata metadata(ValidationCode code) {
        var metadata = new Metadata();
        var errorMsg = ErrorMessage.newBuilder().setValidationCode(code).build();
        metadata.put(METADATA_KEY, errorMsg);
//        Metadata.Key<String> key = Metadata.Key.of("INVALID_ACCOUNT", Metadata.ASCII_STRING_MARSHALLER);
//        metadata.put(key, code.toString());
        /**
         * value are sent in binary format
         * to send in string format use
         *
         * Metadata.Key<String> key = Metadata.Key.of("INVALID_ACCOUNT", Metadata.ASCII_STRING_MARSHALLER);
         * metadata.put(key, code.toString());
         */
        return metadata;
    }
}
