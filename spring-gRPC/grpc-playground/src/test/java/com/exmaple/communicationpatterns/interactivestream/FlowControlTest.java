package com.exmaple.communicationpatterns.interactivestream;

import com.example.common.GrpcServer;
import com.example.communicationpatterns.interactivestream.FlowControlServiceGrpc;
import com.example.communicationpatterns06.interactivestream.FlowControlService;
import com.exmaple.common.AbstractChannelTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FlowControlTest extends AbstractChannelTest {

    private final GrpcServer server = GrpcServer.create(new FlowControlService());
    private FlowControlServiceGrpc.FlowControlServiceStub stub;

    @BeforeAll
    public void setup(){
        this.server.start();
        this.stub = FlowControlServiceGrpc.newStub(channel);
    }

    @Test
    public void flowControlTest(){
        var responseObserver = new ResponseHandler();
        var requestObserver = this.stub.getMessages(responseObserver);
        responseObserver.setRequestObserver(requestObserver);
        responseObserver.start(5);
        responseObserver.await();
    }

    @AfterAll
    public void stop(){
        this.server.stop();
    }

}
