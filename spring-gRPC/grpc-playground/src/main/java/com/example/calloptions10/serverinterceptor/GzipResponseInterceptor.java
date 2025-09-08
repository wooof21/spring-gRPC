package com.example.calloptions10.serverinterceptor;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import org.springframework.stereotype.Component;

@Component
public class GzipResponseInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall,
                                                                 Metadata metadata,
                                                                 ServerCallHandler<ReqT, RespT> serverCallHandler) {
        // global config server use gzip compression for all responses
        // server interceptors will be configured on the GrpcServer class
        serverCall.setCompression("gzip");
        return serverCallHandler.startCall(serverCall, metadata);
    }

}
