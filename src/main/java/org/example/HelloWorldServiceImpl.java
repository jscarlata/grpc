package org.example;

import helloworld.Hello;
import helloworld.HelloWorldServiceGrpc;
import io.grpc.stub.StreamObserver;

public class HelloWorldServiceImpl
        extends HelloWorldServiceGrpc.HelloWorldServiceImplBase {

    @Override
    public void hello(
            Hello.HelloRequest request,
            StreamObserver<Hello.HelloResponse> responseObserver) {
        // Generate a greeting message for the original method
        Hello.HelloResponse reply = Hello.HelloResponse.newBuilder().setText("Hello " + request.getText()).build();
        // Send the reply back to the client.
        responseObserver.onNext(reply);
        // Indicate that no further messages will be sent to the client.
        responseObserver.onCompleted();
    }
}