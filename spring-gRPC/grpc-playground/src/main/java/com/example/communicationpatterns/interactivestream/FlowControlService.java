package com.example.communicationpatterns.interactivestream;

import com.example.common.GrpcService;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.stream.IntStream;

@Slf4j
@Service
public class FlowControlService extends FlowControlServiceGrpc.FlowControlServiceImplBase implements GrpcService {

    @Override
    public StreamObserver<RequestSize> getMessages(StreamObserver<Output> responseObserver) {
        return new RequestHandler(responseObserver);
    }

    private static class RequestHandler implements StreamObserver<RequestSize> {

        private final StreamObserver<Output> responseObserver;
        private Integer emitCount; // to keep track the number of messages emitted so far!

        public RequestHandler(StreamObserver<Output> responseObserver) {
            this.responseObserver = responseObserver;
            this.emitCount = 0;
        }

        @Override
        public void onNext(RequestSize requestSize) {
            IntStream.rangeClosed((emitCount + 1), 100)
                    .limit(requestSize.getSize())
                    .forEach(i -> {
                        log.info("Emitting {}", i);
                        responseObserver.onNext(Output.newBuilder().setValue(i).build());
                    });
            emitCount += requestSize.getSize();
            if(emitCount >= 100) {
                responseObserver.onCompleted();
            }
        }

        @Override
        public void onError(Throwable throwable) {

        }

        @Override
        public void onCompleted() {
           this.responseObserver.onCompleted();
        }
    }

}
