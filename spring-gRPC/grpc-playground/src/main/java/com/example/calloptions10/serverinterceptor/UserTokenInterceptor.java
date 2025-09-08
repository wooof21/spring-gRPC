package com.example.calloptions10.serverinterceptor;

import com.example.calloptions10.Constants;
import com.example.calloptions10.UserRole;
import io.grpc.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static com.example.calloptions10.Constants.PRIME_SET;
import static com.example.calloptions10.Constants.STANDARD_SET;

/**
 * user-token-3, user-token-4 => prime users, all calls are allowed
 * user-token-5, user-token-6 => standard users, server streaming calls(Withdraw) are NOT allowed.
 * other calls(GetAccountBalance) are allowed.
 * any other token            => not valid...!
 *
 * additional business requirement on GetAccountBalance:
 *  - prime users: return the balance as it is
 *  - standard users, deduct $1 and then return the balance
 *
 * When there are some business requirements like above, it cannot be implemented in the interceptor,
 * since the business logic should be implemented in the service layer
 *
 * To pass the user role from interceptor to service layer, use `Context`
 *  - toContext: put the user role header into the Context
 *   - in the service layer, extract the user role from the Context
 */

@Slf4j
@Component
public class UserTokenInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall,
                                                                 Metadata metadata,
                                                                 ServerCallHandler<ReqT, RespT> serverCallHandler) {

        var token = extractToken(metadata.get(Constants.USER_TOKEN_KEY));
        log.info("{}", token);
        var ctx = toContext(token);
        if(!isValid(token)) {
            return close(serverCall, metadata, Status.UNAUTHENTICATED
                                                .withDescription("Token is either null or invalid"));
        }
        // check which call is allowed -> streaming vs unary
        var isOneMessage = serverCall.getMethodDescriptor().getType().serverSendsOneMessage();
        if(isOneMessage || PRIME_SET.contains(token)) {
            if(Objects.nonNull(ctx)) {
                // to pass the modified Context to the service layer
                return Contexts.interceptCall(ctx, serverCall, metadata, serverCallHandler);
            }
            // for unmodified Context
            return serverCallHandler.startCall(serverCall, metadata);
        }
        return close(serverCall, metadata, Status.PERMISSION_DENIED
                                            .withDescription("User is not allowed to do this operation"));
    }

    private String extractToken(String value) {
        return Objects.nonNull(value) && value.startsWith(Constants.BEARER) ?
                value.substring(Constants.BEARER.length()).trim() : null;
    }

    private boolean isValid(String token) {
        return Objects.nonNull(token) && (PRIME_SET.contains(token) || STANDARD_SET.contains(token));
    }

    private Context toContext(String token){
        if(Objects.nonNull(token) && (PRIME_SET.contains(token) || STANDARD_SET.contains(token))) {
            var role = PRIME_SET.contains(token) ? UserRole.PRIME : UserRole.STANDARD;
            return Context.current().withValue(Constants.USER_ROLE_KEY, role);
        }
        return null;
    }

    private <ReqT, RespT> ServerCall.Listener<ReqT> close(ServerCall<ReqT, RespT> serverCall,
                                                          Metadata metadata,
                                                          Status status) {
        serverCall.close(status, metadata);
        return new ServerCall.Listener<ReqT>() {
        };
    }

}
