package org.example;

import helloworld.HelloWorldServiceGrpc;
import helloworld.Hello;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.util.logging.Level;
import java.util.logging.Logger;

public class HelloWorldClient {
    private static final Logger logger = Logger.getLogger(HelloWorldClient.class.getName());

    // connection
    private final ManagedChannel channel;
    private final HelloWorldServiceGrpc.HelloWorldServiceBlockingStub blockingStub;

    public HelloWorldClient(String host, int port) {
        channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        blockingStub = HelloWorldServiceGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS);
    }

    public void greet(String name) {
        // Log a message indicating the intention to greet a user.
        logger.info("Will try to greet " + name + " ...");

        // Creating a request with the user's name.
        Hello.HelloRequest request = Hello.HelloRequest.newBuilder().setText(name).build();
        Hello.HelloResponse response;
        try {
            // Call the original method on the server.
            response = blockingStub.hello(request);
        } catch (StatusRuntimeException e) {
            // Log a warning if the RPC fails.
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return;
        }

        logger.info("Greeting: " + response.getText());
    }

    public static void main(String[] args) throws Exception {
        HelloWorldClient client = new HelloWorldClient("localhost", 50051);
        try {
            String user = "World";
            client.greet(user);
        } finally {
            client.shutdown();
        }
    }
}