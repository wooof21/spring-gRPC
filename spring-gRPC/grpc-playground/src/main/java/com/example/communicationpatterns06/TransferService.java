package com.example.communicationpatterns06;

import com.example.common.GrpcService;
import com.example.communicationpatterns.TransferRequest;
import com.example.communicationpatterns.TransferResponse;
import com.example.communicationpatterns.TransferServiceGrpc;
import com.example.communicationpatterns06.requesthandlers.TransferRequestHandler;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

@Service
public class TransferService extends TransferServiceGrpc.TransferServiceImplBase implements GrpcService {

    // bidirectional streaming
    @Override
    public StreamObserver<TransferRequest> transfer(StreamObserver<TransferResponse> responseObserver) {
        return new TransferRequestHandler(responseObserver);
    }

}
