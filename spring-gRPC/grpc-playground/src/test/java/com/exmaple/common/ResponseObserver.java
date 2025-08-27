package com.exmaple.common;

import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/*
    Test utility
 */

public class ResponseObserver<T> implements StreamObserver<T> {

    private static final Logger log = LoggerFactory.getLogger(ResponseObserver.class);

    private final List<T> list = new ArrayList<>();
    private final CountDownLatch latch;
    private Throwable throwable;

    private ResponseObserver(int countDown){
        this.latch = new CountDownLatch(countDown);
    }

    public static <T> ResponseObserver<T> create(){
        return new ResponseObserver<>(1);
    }

    public static <T> ResponseObserver<T> create(int countDown){
        return new ResponseObserver<>(countDown);
    }

    @Override
    public void onNext(T t) {
        log.info("Received item: {}", t);
        this.list.add(t);
    }

    @Override
    public void onError(Throwable throwable) {
        log.error("Received error: {}", throwable.getMessage());
        this.throwable = throwable;
        this.latch.countDown();
    }

    @Override
    public void onCompleted() {
        log.info("Completed");
        this.latch.countDown();
    }

    public void await() {
        try {
            this.latch.await(3, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<T> getItems(){
        return this.list;
    }

    public Throwable getError() {
        return this.throwable;
    }

    }
