package com.exmaple.communicationpatterns.interactivestream;

import com.example.communicationpatterns.interactivestream.Output;
import com.example.communicationpatterns.interactivestream.RequestSize;
import com.google.common.util.concurrent.Uninterruptibles;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class ResponseHandler implements StreamObserver<Output> {

    private static final Logger log = LoggerFactory.getLogger(ResponseHandler.class);

    private final CountDownLatch latch = new CountDownLatch(1);
    private StreamObserver<RequestSize> requestObserver;
    private int size;

    @Override
    public void onNext(Output output) {
        this.size--;
        this.process(output);
        if(this.size == 0) {
            int newSize = ThreadLocalRandom.current().nextInt(1, 10);
            log.info("Process complete - Requesting new size: {}", newSize);
            this.request(newSize);
        }
    }

    @Override
    public void onError(Throwable throwable) {
        latch.countDown();
    }

    @Override
    public void onCompleted() {
        this.requestObserver.onCompleted();
        log.info("Test - Completed");
        latch.countDown();
    }

    public void setRequestObserver(StreamObserver<RequestSize> requestObserver) {
        this.requestObserver = requestObserver;
    }

    private void request(int size){
        log.info("Test - Requesting size: {}", size);
        this.size = size;
        this.requestObserver.onNext(RequestSize.newBuilder().setSize(size).build());
    }

    private void process(Output output){
        log.info("Test - Received: {}", output);
        // random delay on the client side
        Uninterruptibles.sleepUninterruptibly(
                ThreadLocalRandom.current().nextInt(50, 200),
                TimeUnit.MILLISECONDS
        );
    }

    public void await(){
        try {
            this.latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void start(int size){
        this.request(size);
    }

}
