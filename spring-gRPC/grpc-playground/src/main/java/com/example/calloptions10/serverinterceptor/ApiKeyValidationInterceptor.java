package com.example.calloptions10.serverinterceptor;

import com.example.calloptions10.Constants;
import io.grpc.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;

// server interceptor to validate api key from client metadata
// remember to register this interceptor with the server
@Slf4j
@Component
public class ApiKeyValidationInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall,
                                                                 Metadata metadata,
                                                                 ServerCallHandler<ReqT, RespT> serverCallHandler) {

        // to only apply the interceptor on specific methods
        // check the method name and skip the rest
        String fullMethodName = serverCall.getMethodDescriptor().getFullMethodName();
        log.info("ApiKeyValidationInterceptor - method name: {}", fullMethodName);

        if(Constants.API_KEY_INTERCEPTOR_APPLY_METHOD.equals(fullMethodName)) {
            var apiKey = metadata.get(Constants.API_KEY);
            log.info("Server interceptor - api key: {}", apiKey);
            if(!isValid(apiKey)){
                serverCall.close(
                        Status.UNAUTHENTICATED.withDescription("Client api key invalid"),
                        metadata
                );
                //return empty ServerCall.Listener
                return new ServerCall.Listener<ReqT>() {};
            }
        }

        return serverCallHandler.startCall(serverCall, metadata);
    }

    private boolean isValid(String apiKey){

        return Objects.nonNull(apiKey) &&
                apiKey.equals("bank-client-secret");
    }

}
