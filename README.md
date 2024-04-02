# grpc

Jakob Scarlata

## Questions

1. **What is gRPC and why does it work across languages and platforms?**
   gRPC is a high-performance, open-source RPC (Remote Procedure Call) framework initially developed by Google. It enables communication between different services in distributed systems. It works across languages and platforms due to its use of Protocol Buffers for defining service contracts and its support for multiple programming languages through code generation.
2. **Describe the RPC life cycle starting with the RPC client?**
    1. The RPC client initiates a remote procedure call by invoking a method on a stub or proxy object.
    2. The stub marshals the method parameters into a request message.
    3. The request message is sent over the network to the RPC server.
    4. The RPC server receives the request, unmarshals the parameters, and executes the corresponding method.
    5. The server marshals the method result into a response message.
    6. The response message is sent back to the client over the network.
    7. The client receives the response, unmarshals the result, and returns it to the caller.
3. **Describe the workflow of Protocol Buffers?**
    1. Define the message structure using a .proto file.
    2. Compile the .proto file using the Protocol Buffers compiler (`protoc`) to generate language-specific code.
    3. Use the generated code to serialize/deserialize data into/from binary format according to the defined message structure.
4. **What are the benefits of using protocol buffers?**
    - Efficient serialization: Protocol Buffers produce smaller and faster serialization compared to XML and JSON.
    - Language and platform independence: Protocol Buffers support multiple programming languages and can be used across different platforms.
    - Versioning and backwards compatibility: Protocol Buffers provide mechanisms for evolving data formats without breaking existing clients or servers.
5. **When is the use of protocol not recommended?**
   Protocol Buffers might not be suitable for situations where human readability of the data format is crucial or where ease of debugging is a priority. Additionally, if real-time data parsing is required and efficiency is not a major concern, simpler formats like JSON might be preferred.
6. **List 3 different data types that can be used with protocol buffers?**
    - Scalar types: int32, float, bool, string, bytes, etc.
    - Enumerations: enum
    - Nested messages: nested message types defined within other message types.

## GKue

Assignment: Make a Hello World Project with gRPC

### 1. Create a proto File

A proto file defines the services which can be used.

For the proto file (hello.proto) I’ve created a new directory in main called proto

I will go over the most important parts of my proto file now:

Defining the syntax of the proto file

```protobuf
syntax = "proto3";
```

Setting the package which I can use later on to export the generated files to

```protobuf
package helloworld;
```

Implementing the service

```protobuf
service HelloWorldService {
  rpc hello(HelloRequest) returns (HelloResponse) {}
}
```

Here I defined the service `HelloWorldService` which has a method `hello` . This method will take a custom attribute `HelloRequest`  and return also a custom attribute `HelloResponse`

The two attributes are defined as following:

```protobuf
message HelloRequest {
  string text = 1;
}

message HelloResponse {
  string text = 1;
}
```

### 2. Adapt the build.gradle

I’m using gradle and therefore I’ll also have to change the build.gradle file.

I added this line to the plugins as I need gRPC:

`id "com.google.protobuf" version "0.9.4"`

I also have following dependencies required:

```protobuf
dependencies {
    // https://mvnrepository.com/artifact/com.google.protobuf/protobuf-java
    implementation group: 'com.google.protobuf', name: 'protobuf-java', version: '3.25.2'
    implementation 'org.springframework.boot:spring-boot-starter'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    runtimeOnly 'io.grpc:grpc-netty-shaded:1.58.0'
    implementation 'io.grpc:grpc-protobuf:1.58.0'
    implementation 'io.grpc:grpc-stub:1.58.0'
    implementation 'net.devh:grpc-spring-boot-starter:2.15.0.RELEASE'
    compileOnly 'org.apache.tomcat:annotations-api:6.0.53' // necessary for Java 9+
}
```

Following section is necessary so my project can identify the generated code in the other project root:

```protobuf
sourceSets {
    main {
        java {
            srcDirs 'build/generated/source/proto/main/java', 'src/main/java'
        }
    }
}
```

Following part will now set the other necessary information for generating the proto tasks:

```protobuf
protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.1"
    }
    plugins {
        grpc {
            artifact = 'io.grpc:protoc-gen-grpc-java:1.61.0'
        }
    }
    generateProtoTasks {
        all()*.plugins {
            grpc {
                outputSubDir = 'java'
            }
        }
    }
}
```

### 3. Build with gradle

Now I will build my project with following command in my terminal

`./gradlew build`

Following files got generated:

`Hello`

`HelloWorldServiceGrpc`

(And many classes contained within them)

### 4. HelloWorldServiceImpl

Now I’m gonna define a new java class which will implement the `HelloWorldService` class.

But first I’ll have to import my generated classes from the package I defined:

```java
import helloworld.Hello;
import helloworld.HelloWorldServiceGrpc;
```

The method head looks like following:

```java
public class HelloWorldServiceImpl
        extends HelloWorldServiceGrpc.HelloWorldServiceImplBase {
```

I will overwrite the `hello` method like following (given important snippets):

```java
// create response variable with user name from request
Hello.HelloResponse reply = Hello.HelloResponse.newBuilder().setText("Hello " + request.getText()).build();

// send response
responseObserver.onNext(reply);
```

### 5. HelloWorldServer

Starting the server with port and my service:

```java
server = ServerBuilder.forPort(PORT)
                .addService(new HelloWorldServiceImpl())
                .build()
                .start();
```

I also defined a main method which will just create a server and start it.

### 6. HelloWorldClient

In my constructor I will init a channel and a blocking stub like following:

```java
channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        blockingStub = HelloWorldServiceGrpc.newBlockingStub(channel);
```

I need the channel for the connection between server and client

My blocking stub will then block my code to the point where it gets called until we get a response from the server.

In my code I use it like following:

`response = blockingStub.hello(request);`