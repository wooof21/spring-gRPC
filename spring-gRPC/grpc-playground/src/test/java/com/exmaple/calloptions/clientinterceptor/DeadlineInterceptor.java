package com.exmaple.calloptions.clientinterceptor;

import io.grpc.*;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

// global deadline interceptor
public class DeadlineInterceptor implements ClientInterceptor {

    private final Duration duration;

    public DeadlineInterceptor(Duration duration) {
        this.duration = duration;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> methodDescriptor,
                                                               CallOptions callOptions,
                                                               Channel channel) {
        // config deadline if not already present
        callOptions = Objects.nonNull(callOptions.getDeadline()) ?
                        callOptions :
                        callOptions.withDeadline(Deadline.after(duration.toMillis(),
                                TimeUnit.MILLISECONDS));
        return channel.newCall(methodDescriptor, callOptions);
    }

}
