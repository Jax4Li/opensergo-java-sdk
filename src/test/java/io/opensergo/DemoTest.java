package io.opensergo;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.NettyServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.util.MutableHandlerRegistry;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.opensergo.proto.transport.v1.*;
import io.opensergo.subscribe.SubscribeKey;
import io.opensergo.subscribe.SubscribeRegistry;
import io.opensergo.subscribe.SubscribedConfigCache;
import io.opensergo.util.IdentifierUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author lihuazeng
 */
public class DemoTest {

    @Test
    public void test() throws InterruptedException, IOException {
        // server
        final MutableHandlerRegistry serviceRegistry = new MutableHandlerRegistry();
        Server server = ServerBuilder.forPort(0).fallbackHandlerRegistry(serviceRegistry).directExecutor().build();
//        NettyServerBuilder.forPort(0).sslContext().
        server.start();
        // implement the fake service
        OpenSergoUniversalTransportServiceGrpc.OpenSergoUniversalTransportServiceImplBase serviceImpl =
                new OpenSergoUniversalTransportServiceGrpc.OpenSergoUniversalTransportServiceImplBase(){
                    @Override
                    public StreamObserver<SubscribeRequest> subscribeConfig(StreamObserver<SubscribeResponse> responseObserver) {
                        return (OpenSergoUniversalTransportServiceTest.FakerStreamObserver) subscribeRequest -> {
                            System.out.println("111111");
                            SubscribeResponse response = SubscribeResponse.newBuilder().setAck(OpenSergoTransportConstants.ACK_FLAG)
                                    .setStatus(Status.newBuilder().setCode(OpenSergoTransportConstants.CODE_SUCCESS)).build();
                            responseObserver.onNext(response);
                            responseObserver.onCompleted();
                        };
                    }
                };
        serviceRegistry.addService(serviceImpl);



        // client
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", server.getPort())
                .usePlaintext()
                .build();
        SubscribedConfigCache configCache = new SubscribedConfigCache();
        SubscribeRegistry subscribeRegistry = new SubscribeRegistry();
        OpenSergoUniversalTransportServiceGrpc.OpenSergoUniversalTransportServiceStub transportGrpcStub = OpenSergoUniversalTransportServiceGrpc.newStub(channel);
        StreamObserver<SubscribeRequest> requestAndResponseWriter = transportGrpcStub.withWaitForReady()
                // The deadline SHOULD be set when waitForReady is enabled
                .withDeadlineAfter(10, TimeUnit.SECONDS).subscribeConfig(new OpenSergoSubscribeClientObserver(configCache, subscribeRegistry));
        SubscribeKey subscribeKey = new SubscribeKey("default", "my-service", ConfigKind.TRAFFIC_ROUTER_STRATEGY);
        SubscribeRequestTarget subTarget = SubscribeRequestTarget.newBuilder()
                .setNamespace(subscribeKey.getNamespace()).setApp(subscribeKey.getApp())
                .addKinds(subscribeKey.getKind().getKindName())
                .build();
        SubscribeRequest request = SubscribeRequest.newBuilder()
                .setRequestId(String.valueOf("1"))
                .setTarget(subTarget).setOpType(SubscribeOpType.SUBSCRIBE)
                .setIdentifier(IdentifierUtils.generateIdentifier(System.identityHashCode(this)))
                .build();
        requestAndResponseWriter.onNext(request);
        Thread.sleep(1000);
    }


    @Test
    public void test2() throws InterruptedException, IOException {
        // server
        final MutableHandlerRegistry serviceRegistry = new MutableHandlerRegistry();

        URL certUrl = getClass().getClassLoader().getResource("cert/server-cert.pem");
        URL privateKeyUrl = getClass().getClassLoader().getResource("cert/server-key.pem");
        URL clientCACertUrl = getClass().getClassLoader().getResource("cert/ca-cert.pem");
        File clientCACertFile = new File(clientCACertUrl.getFile());
        SslContext sslContext = GrpcSslContexts.configure(SslContextBuilder.forServer(new File(certUrl.getFile()), new File(privateKeyUrl.getFile())).trustManager(clientCACertFile)).build();
        Server server = NettyServerBuilder.forPort(0).sslContext(sslContext).fallbackHandlerRegistry(serviceRegistry).directExecutor().build();
        server.start();
        // implement the fake service
        OpenSergoUniversalTransportServiceGrpc.OpenSergoUniversalTransportServiceImplBase serviceImpl =
                new OpenSergoUniversalTransportServiceGrpc.OpenSergoUniversalTransportServiceImplBase(){
                    @Override
                    public StreamObserver<SubscribeRequest> subscribeConfig(StreamObserver<SubscribeResponse> responseObserver) {
                        System.out.println("0000");
                        return (OpenSergoUniversalTransportServiceTest.FakerStreamObserver) subscribeRequest -> {
                            System.out.println("111111");
                            SubscribeResponse response = SubscribeResponse.newBuilder().setAck(OpenSergoTransportConstants.ACK_FLAG)
                                    .setStatus(Status.newBuilder().setCode(OpenSergoTransportConstants.CODE_SUCCESS)).build();
                            responseObserver.onNext(response);
                            responseObserver.onCompleted();
                        };
                    }
                };
        serviceRegistry.addService(serviceImpl);



        // client
//        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", server.getPort())
//                .usePlaintext()
//                .build();
        URL caUrl = getClass().getClassLoader().getResource("cert/ca-cert.pem");
        URL clientCertUrl = getClass().getClassLoader().getResource("cert/client-cert.pem");
        URL clientKeyUrl = getClass().getClassLoader().getResource("cert/client-key.pem");
        File clientCertFile = new File(clientCertUrl.getFile());
        File clientKeyFile = new File(clientKeyUrl.getFile());
        File serverCACertFile = new File(caUrl.getFile());
        SslContext sslContext2 = GrpcSslContexts.configure(SslContextBuilder.forClient().keyManager(clientCertFile, clientKeyFile).trustManager(serverCACertFile)).build();
        ManagedChannel channel = NettyChannelBuilder.forAddress("localhost", server.getPort())
                .useTransportSecurity()
                .sslContext(sslContext2)
                .build();
        SubscribedConfigCache configCache = new SubscribedConfigCache();
        SubscribeRegistry subscribeRegistry = new SubscribeRegistry();
        OpenSergoUniversalTransportServiceGrpc.OpenSergoUniversalTransportServiceStub transportGrpcStub = OpenSergoUniversalTransportServiceGrpc.newStub(channel);
        StreamObserver<SubscribeRequest> requestAndResponseWriter = transportGrpcStub.withWaitForReady()
                // The deadline SHOULD be set when waitForReady is enabled
                .withDeadlineAfter(10, TimeUnit.SECONDS).subscribeConfig(new OpenSergoSubscribeClientObserver(configCache, subscribeRegistry));
        SubscribeKey subscribeKey = new SubscribeKey("default", "my-service", ConfigKind.TRAFFIC_ROUTER_STRATEGY);
        SubscribeRequestTarget subTarget = SubscribeRequestTarget.newBuilder()
                .setNamespace(subscribeKey.getNamespace()).setApp(subscribeKey.getApp())
                .addKinds(subscribeKey.getKind().getKindName())
                .build();
        SubscribeRequest request = SubscribeRequest.newBuilder()
                .setRequestId(String.valueOf("3"))
                .setTarget(subTarget).setOpType(SubscribeOpType.SUBSCRIBE)
                .setIdentifier(IdentifierUtils.generateIdentifier(System.identityHashCode(this)))
                .build();
        requestAndResponseWriter.onNext(request);
        Thread.sleep(3000);
    }

    @Test
    public void test3() throws InterruptedException, IOException {

        Logger logger = Logger.getLogger("io.grpc");
        logger.setLevel(Level.ALL);
        logger.addHandler(new ConsoleHandler());

        URL caUrl = getClass().getClassLoader().getResource("cert/ca-cert.pem");

        // implement the fake service
        OpenSergoUniversalTransportServiceGrpc.OpenSergoUniversalTransportServiceImplBase serviceImpl =
                new OpenSergoUniversalTransportServiceGrpc.OpenSergoUniversalTransportServiceImplBase(){
                    @Override
                    public StreamObserver<SubscribeRequest> subscribeConfig(StreamObserver<SubscribeResponse> responseObserver) {
                        System.out.println("0000");
                        return (OpenSergoUniversalTransportServiceTest.FakerStreamObserver) subscribeRequest -> {
                            System.out.println("111111");
                            SubscribeResponse response = SubscribeResponse.newBuilder().setAck(OpenSergoTransportConstants.ACK_FLAG)
                                    .setStatus(Status.newBuilder().setCode(OpenSergoTransportConstants.CODE_SUCCESS)).build();
                            responseObserver.onNext(response);
                            responseObserver.onCompleted();
                        };
                    }
                };
        URL certUrl = getClass().getClassLoader().getResource("cert/server-cert.pem");
        URL privateKeyUrl = getClass().getClassLoader().getResource("cert/server-key.pem");
        Server server = NettyServerBuilder.forPort(50051)
                .addService(serviceImpl)
                .sslContext(GrpcSslContexts.forServer(new File(certUrl.getFile()), new File(privateKeyUrl.getFile())).build())
                .build();
        server.start();

//        SslContextBuilder sslContextBuilder = SslContextBuilder.forServer(new File(certUrl.getFile()), new File(privateKeyUrl.getFile()));
//        SslContext sslContext = GrpcSslContexts.configure(sslContextBuilder).build();
//
//        server = NettyServerBuilder.forPort(50051)
//                .sslContext(sslContext)
//                .addService(serviceImpl)
//                .build();


        URL clientCertUrl = getClass().getClassLoader().getResource("cert/client-cert.pem");
        URL clientKeyUrl = getClass().getClassLoader().getResource("cert/client-key.pem");
        File clientCertFile = new File(clientCertUrl.getFile());
        File clientKeyFile = new File(clientKeyUrl.getFile());
        SslContext sslContext = GrpcSslContexts.forClient()
//                .keyManager(clientCertFile, clientKeyFile, null)
                .trustManager(new File(caUrl.getFile()))
//                .trustManager(new File(certUrl.getFile()))
                .build();
        ManagedChannel channel = NettyChannelBuilder.forAddress("0.0.0.0", 50051)
//                .usePlaintext()
                .sslContext(sslContext)
                .build();

        OpenSergoUniversalTransportServiceGrpc.OpenSergoUniversalTransportServiceStub stub = OpenSergoUniversalTransportServiceGrpc.newStub(channel);
        StreamObserver<SubscribeResponse> responseObserver = new StreamObserver<SubscribeResponse>() {
            @Override
            public void onNext(SubscribeResponse o) {
                System.out.println(2222);
                onCompleted();
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onCompleted() {

            }
        };

        StreamObserver<SubscribeRequest> requestObserver = stub.subscribeConfig(responseObserver);
        SubscribeRequest request = SubscribeRequest.newBuilder()
                .setRequestId(String.valueOf("3"))
                .setIdentifier(IdentifierUtils.generateIdentifier(System.identityHashCode(this)))
                .build();
        requestObserver.onNext(request);

        Thread.sleep(3000);
    }

    @Test
    public void test4() throws InterruptedException, IOException {
        // implement the fake service
        OpenSergoUniversalTransportServiceGrpc.OpenSergoUniversalTransportServiceImplBase serviceImpl =
                new OpenSergoUniversalTransportServiceGrpc.OpenSergoUniversalTransportServiceImplBase(){
                    @Override
                    public StreamObserver<SubscribeRequest> subscribeConfig(StreamObserver<SubscribeResponse> responseObserver) {
                        System.out.println("0000");
                        return (OpenSergoUniversalTransportServiceTest.FakerStreamObserver) subscribeRequest -> {
                            System.out.println("111111");
                            SubscribeResponse response = SubscribeResponse.newBuilder().setAck(OpenSergoTransportConstants.ACK_FLAG)
                                    .setStatus(Status.newBuilder().setCode(OpenSergoTransportConstants.CODE_SUCCESS)).build();
                            responseObserver.onNext(response);
                            responseObserver.onCompleted();
                        };
                    }
                };
        URL rootCertUrl = getClass().getClassLoader().getResource("cert2/root.crt");
        URL certUrl = getClass().getClassLoader().getResource("cert2/cert.crt");// a.internal-api.test.froxieprice.com
        URL privateKeyUrl = getClass().getClassLoader().getResource("cert2/server-pkcs8.pem");
        Server server = NettyServerBuilder.forPort(50051)
                .addService(serviceImpl)
                .sslContext(GrpcSslContexts.forServer(new File(certUrl.getFile()), new File(privateKeyUrl.getFile())).build())
                .build();
        server.start();

        SslContext sslContext = GrpcSslContexts.forClient()
//                .keyManager(clientCertFile, clientKeyFile)
//                .trustManager(new File(certUrl.getFile()))
                .trustManager(new File(rootCertUrl.getFile()))
                .build();
        ManagedChannel channel = NettyChannelBuilder.forAddress("a.internal-api.test.froxieprice.com", 50051)
                .sslContext(sslContext)
                .build();

//        NettyChannelBuilder.forAddress("a.internal-api.test.froxieprice.com", 50051).maxInboundMessageSize().maxRetryAttempts().maxHedgedAttempts()
//                .retryBufferSize().perRpcBufferLimit().idleTimeout().keepAliveTime().keepAliveTimeout().keepAliveWithoutCalls()

        OpenSergoUniversalTransportServiceGrpc.OpenSergoUniversalTransportServiceStub stub = OpenSergoUniversalTransportServiceGrpc.newStub(channel);
        StreamObserver<SubscribeResponse> responseObserver = new StreamObserver<SubscribeResponse>() {
            @Override
            public void onNext(SubscribeResponse o) {
                System.out.println(2222);
                onCompleted();
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onCompleted() {

            }
        };

        StreamObserver<SubscribeRequest> requestObserver = stub.subscribeConfig(responseObserver);
        SubscribeRequest request = SubscribeRequest.newBuilder()
                .setRequestId(String.valueOf("3"))
                .setIdentifier(IdentifierUtils.generateIdentifier(System.identityHashCode(this)))
                .build();
        requestObserver.onNext(request);

        Thread.sleep(3000);
    }
}
