package com.exmaple.communicationpatterns.bidirguessnumber;

import com.example.common.GrpcServer;
import com.example.communicationpatterns.bidirguessnumber.GuessNumberGrpc;
import com.example.communicationpatterns06.bidirguessnumber.GuessNumberService;
import com.exmaple.common.AbstractChannelTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GuessANumberTest extends AbstractChannelTest {

    private static final Logger log = LoggerFactory.getLogger(GuessANumberTest.class);
    private final GrpcServer server = GrpcServer.create(new GuessNumberService());
    private GuessNumberGrpc.GuessNumberStub stub;

    @BeforeAll
    public void setup(){
        this.server.start();
        this.stub = GuessNumberGrpc.newStub(channel);
    }

    @RepeatedTest(5)
    public void guessANumberGame(){
        log.info("------- New Game -------");
        var responseObserver = new GuessResponseHandler();
        var requestObserver = this.stub.makeGuess(responseObserver);
        responseObserver.setRequestObserver(requestObserver);
        responseObserver.start();
        responseObserver.await();
    }

    @AfterAll
    public void stop(){
        this.server.stop();
    }

}
