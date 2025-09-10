package com.trading.aggregator.controller;

import com.trading.aggregator.service.StockPriceUpdateListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/stock")
public class StockController {


    @Autowired
    private StockPriceUpdateListener listener;

    /**
     * SseEmitter: Server-Sent Events (SSE)
     *  * is a standard allowing servers to push real-time updates to clients over HTTP.
     *  * is a Spring class that facilitates this by managing the connection and allowing
     *      the server to send events.
     *  * It is typically used for real-time notifications, live updates,
     *      or streaming data to web clients.
     */
    @GetMapping(value = "updates", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter priceStreaming(){
        return listener.registerClient();
    }
}
