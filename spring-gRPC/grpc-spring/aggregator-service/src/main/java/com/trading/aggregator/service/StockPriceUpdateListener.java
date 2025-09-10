package com.trading.aggregator.service;

import com.trading.aggregator.model.PriceUpdateModel;
import com.trading.stock.PriceUpdate;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Service
@Slf4j
public class StockPriceUpdateListener implements StreamObserver<PriceUpdate> {

    private final Set<SseEmitter> emitters = new CopyOnWriteArraySet<>();
    private final long sseTimeout;

    public StockPriceUpdateListener(@Value("${sse.timeout:300000}") long sseTimeout) {
        this.sseTimeout = sseTimeout;
    }

    public SseEmitter registerClient(){
        SseEmitter emitter = new SseEmitter(this.sseTimeout);
        this.emitters.add(emitter);
        // remove emitter when its timeout, completes or errors out
        emitter.onTimeout(() -> this.emitters.remove(emitter));
        emitter.onCompletion(() -> this.emitters.remove(emitter));
        emitter.onError(e -> this.emitters.remove(emitter));
        return emitter;
    }

    @Override
    public void onNext(PriceUpdate priceUpdate) {
        var model = new PriceUpdateModel(priceUpdate.getStock().toString(), priceUpdate.getPrice());
        // remove emitter if sending event fails
        this.emitters.removeIf(sse -> !this.sendEvent(sse, model));
    }

    @Override
    public void onError(Throwable throwable) {
        log.error("Streaming error: ", throwable);
        this.emitters.forEach(sse -> sse.completeWithError(throwable));
        this.emitters.clear();
    }

    @Override
    public void onCompleted() {
        this.emitters.forEach(ResponseBodyEmitter::complete);
        this.emitters.clear();
    }

    private boolean sendEvent(SseEmitter emitter, PriceUpdateModel model){
        try {
//            emitter.send(model);
            emitter.send(SseEmitter.event()
                    .name("price-update")
                    .data(model));
            return true;
        } catch (Exception e) {
            log.error("Error sending event to emitter: ", e);
            return false;
        }
    }
}
