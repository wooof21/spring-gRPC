package com.example.communicationpatterns06.bidirguessnumber;

import com.example.common.GrpcService;
import com.example.communicationpatterns.bidirguessnumber.GuessNumberGrpc;
import com.example.communicationpatterns.bidirguessnumber.GuessRequest;
import com.example.communicationpatterns.bidirguessnumber.GuessResponse;
import com.example.communicationpatterns.bidirguessnumber.Result;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GuessNumberService extends GuessNumberGrpc.GuessNumberImplBase implements GrpcService {

    @Override
    public StreamObserver<GuessRequest> makeGuess(StreamObserver<GuessResponse> responseObserver) {
        return new GuessRequestHandler(responseObserver);
    }

    private static class GuessRequestHandler implements StreamObserver<GuessRequest> {

        private final StreamObserver<GuessResponse> responseObserver;
        private final int target;
        private int attempt;

        public GuessRequestHandler(StreamObserver<GuessResponse> responseObserver) {
            this.responseObserver = responseObserver;
            this.attempt = 0;
            this.target = (int) (Math.random() * 100) + 1;
        }

        @Override
        public void onNext(GuessRequest guessRequest) {
            if(guessRequest.getGuess() > target) {
                this.send(Result.TOO_HIGH);
            } else if(guessRequest.getGuess() < target) {
                this.send(Result.TOO_LOW);
            } else {
                log.info("client guess {} is correct", guessRequest.getGuess());
                this.send(Result.CORRECT);
                this.responseObserver.onCompleted();
            }
        }

        @Override
        public void onError(Throwable throwable) {
            log.error("Error: {}", throwable.getMessage());
        }

        @Override
        public void onCompleted() {
            this.responseObserver.onCompleted();
        }

        private void send(Result result){
            attempt++;
            var response = GuessResponse.newBuilder()
                                        .setAttempt(attempt)
                                        .setResult(result)
                                        .build();
            responseObserver.onNext(response);
        }
    }
}
