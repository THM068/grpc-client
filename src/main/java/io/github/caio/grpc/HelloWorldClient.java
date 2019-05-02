package io.github.caio.grpc;


//import helloworld.grpc.AuthTokenProvideInterceptor;
import helloworld.grpc.AuthTokenProvideInterceptor;
import helloworld.grpc.HelloRequest;
import helloworld.grpc.HelloResponse;
import helloworld.grpc.HelloServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HelloWorldClient {
    private static final Logger logger = Logger.getLogger(HelloWorldClient.class.getName());

    private final ManagedChannel channel;
    protected HelloServiceGrpc.HelloServiceBlockingStub blockingStub;

    public HelloWorldClient(String hostname, int port) {
        channel = ManagedChannelBuilder.forAddress(hostname, port)
                .usePlaintext(true)
                .build();
        blockingStub = HelloServiceGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void greet(String name) {
        logger.info("Trying to greet " + name);
        try {
            HelloRequest request = HelloRequest.newBuilder().setFirstName(name).build();
            HelloResponse response = blockingStub.hello(request);
            logger.info("Response: " + response.getGreeting());
        } catch (RuntimeException e) {
            logger.log(Level.WARNING, "Request to grpc server failed", e);
        }
    }


    public static void main(String[] args) throws Exception {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .intercept(new AuthTokenProvideInterceptor())
                .build();

        HelloServiceGrpc.HelloServiceBlockingStub stub
                = HelloServiceGrpc.newBlockingStub(channel);
        try {
            HelloResponse helloResponse = stub.hello(HelloRequest.newBuilder()
                    .setFirstName("Baeldung")
                    .setLastName("gRPC")
                    .build());
            System.out.println(helloResponse.getGreeting());
        }
        catch (StatusRuntimeException ex) {
            System.out.println("Error code ");
            ex.printStackTrace();
        }


        channel.shutdown();
    }
}
