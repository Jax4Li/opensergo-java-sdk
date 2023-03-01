/*
 * Copyright 2022, OpenSergo Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.opensergo;

import com.google.protobuf.Any;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;
import io.grpc.util.MutableHandlerRegistry;
import io.opensergo.log.OpenSergoLogger;
import io.opensergo.proto.fault_tolerance.v1.FaultToleranceRule;
import io.opensergo.proto.transport.v1.*;
import io.opensergo.subscribe.OpenSergoConfigSubscriber;
import io.opensergo.subscribe.SubscribeKey;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.*;

/**
 * @author Eric Zhao
 */
@RunWith(JUnit4.class)
public class OpenSergoClientManagerTest {
    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    private final MutableHandlerRegistry serviceRegistry = new MutableHandlerRegistry();
    private OpenSergoClient client;

    @Before
    public void setup() throws Exception {
        Server server = ServerBuilder.forPort(0).fallbackHandlerRegistry(serviceRegistry).directExecutor().build();
        grpcCleanup.register(server.start());
        client = OpenSergoClientManager.get().getOrCreateClient("localhost", server.getPort());
        client.start();
    }


    @Test
    public void testGetOrCreateClientDefault() {
        OpenSergoClientManager manager = new OpenSergoClientManager();
        OpenSergoClient client1 = manager.getOrCreateClient("127.0.0.1", 12345);
        OpenSergoClient client2 = manager.getOrCreateClient("127.0.0.1", 12345);
        OpenSergoClient client3 = manager.getOrCreateClient("1.2.3.4", 12345);

        assertSame(client1, client2);
        assertNotEquals(client1, client3);
    }

    @Test
    public void testSubscribeConfigServerResponseSuccess() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        final SubscribeRequest[] actualRequests = new SubscribeRequest[1];

        // implement the fake service
        OpenSergoUniversalTransportServiceGrpc.OpenSergoUniversalTransportServiceImplBase serviceImpl =
                new OpenSergoUniversalTransportServiceGrpc.OpenSergoUniversalTransportServiceImplBase(){
                    @Override
                    public StreamObserver<SubscribeRequest> subscribeConfig(StreamObserver<SubscribeResponse> responseObserver) {

                        return new StreamObserver<SubscribeRequest>() {
                            @Override
                            public void onNext(SubscribeRequest subscribeRequest) {
                                countDownLatch.countDown();
                                actualRequests[0] = subscribeRequest;
                                SubscribeResponse response = SubscribeResponse.newBuilder().setAck(OpenSergoTransportConstants.ACK_FLAG)
                                        .setStatus(Status.newBuilder().setCode(OpenSergoTransportConstants.CODE_SUCCESS)).build();
                                responseObserver.onNext(response);
                            }

                            @Override
                            public void onError(Throwable throwable) {

                            }

                            @Override
                            public void onCompleted() {

                            }
                        };
                    }
                };
        serviceRegistry.addService(serviceImpl);

        // client call service
        SubscribeKey subscribeKey = new SubscribeKey("default", "my-service", ConfigKind.RATE_LIMIT_STRATEGY);
        client.subscribeConfig(subscribeKey);
        countDownLatch.await();
        assertNotNull(actualRequests[0]);
        assertNotNull(actualRequests[0].getTarget());
        assertEquals(subscribeKey.getApp(), actualRequests[0].getTarget().getApp());
        assertEquals(subscribeKey.getNamespace(), actualRequests[0].getTarget().getNamespace());
        assertEquals(subscribeKey.getKind().getKindName(), actualRequests[0].getTarget().getKinds(0));
    }

    @Test
    public void testSubscribeConfigServerResponseError() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        final SubscribeRequest[] actualRequests = new SubscribeRequest[1];

        // implement the fake service
        OpenSergoUniversalTransportServiceGrpc.OpenSergoUniversalTransportServiceImplBase serviceImpl =
                new OpenSergoUniversalTransportServiceGrpc.OpenSergoUniversalTransportServiceImplBase(){
                    @Override
                    public StreamObserver<SubscribeRequest> subscribeConfig(StreamObserver<SubscribeResponse> responseObserver) {

                        return new StreamObserver<SubscribeRequest>() {
                            @Override
                            public void onNext(SubscribeRequest subscribeRequest) {
                                actualRequests[0] = subscribeRequest;
                                SubscribeResponse response = SubscribeResponse.newBuilder().setAck(OpenSergoTransportConstants.ACK_FLAG)
                                        .setStatus(Status.newBuilder().setCode(OpenSergoTransportConstants.CODE_ERROR_SUBSCRIBE_HANDLER_ERROR)).build();
                                responseObserver.onNext(response);
                                countDownLatch.countDown();
                            }

                            @Override
                            public void onError(Throwable throwable) {

                            }

                            @Override
                            public void onCompleted() {

                            }
                        };
                    }
                };
        serviceRegistry.addService(serviceImpl);

        // client call service
        SubscribeKey subscribeKey = new SubscribeKey("default", "my-service", ConfigKind.FAULT_TOLERANCE_RULE);
        client.subscribeConfig(subscribeKey);
        countDownLatch.await();
        assertNotNull(actualRequests[0]);
        assertNotNull(actualRequests[0].getTarget());
        assertEquals(subscribeKey.getApp(), actualRequests[0].getTarget().getApp());
        assertEquals(subscribeKey.getNamespace(), actualRequests[0].getTarget().getNamespace());
        assertEquals(subscribeKey.getKind().getKindName(), actualRequests[0].getTarget().getKinds(0));
    }

    @Test
    public void testServerPushData() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        final Object[] actualDatas = new Object[1];
        final SubscribeRequest[] actualRequests = new SubscribeRequest[1];
        FaultToleranceRule.FaultToleranceRuleTargetRef ruleTargetRef = FaultToleranceRule.FaultToleranceRuleTargetRef.newBuilder()
                .setTargetResourceName("/foo")
                .build();
        FaultToleranceRule.FaultToleranceStrategyRef strategyRef = FaultToleranceRule.FaultToleranceStrategyRef.newBuilder()
                .setKind("RateLimitStrategy")
                .setName("rate-limit-foo")
                .build();
        FaultToleranceRule.FaultToleranceActionRef actionRef = FaultToleranceRule.FaultToleranceActionRef.newBuilder()
                .setKind("HttpRequestFallbackAction")
                .setName("fallback-foo")
                .build();

        // implement the fake service
        OpenSergoUniversalTransportServiceGrpc.OpenSergoUniversalTransportServiceImplBase serviceImpl =
                new OpenSergoUniversalTransportServiceGrpc.OpenSergoUniversalTransportServiceImplBase(){
                    @Override
                    public StreamObserver<SubscribeRequest> subscribeConfig(StreamObserver<SubscribeResponse> responseObserver) {

                        return new StreamObserver<SubscribeRequest>() {
                            @Override
                            public void onNext(SubscribeRequest subscribeRequest) {
                                actualRequests[0] = subscribeRequest;
                                SubscribeResponse response = SubscribeResponse.newBuilder().setAck(OpenSergoTransportConstants.ACK_FLAG)
                                        .setStatus(Status.newBuilder().setCode(OpenSergoTransportConstants.CODE_SUCCESS)).build();
                                responseObserver.onNext(response);
//                                Thread thread = new Thread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        try {
//                                            FaultToleranceRule rule = FaultToleranceRule.newBuilder()
//                                                    .setAction(actionRef)
//                                                    .addStrategies(strategyRef)
//                                                    .addTargets(ruleTargetRef)
//                                                    .build();
//                                            DataWithVersion dataWithVersion = DataWithVersion.newBuilder().setVersion(0)
//                                                    .addData(Any.newBuilder().setValue(rule.toByteString()).build()).build();
//                                            SubscribeResponse response = SubscribeResponse.newBuilder()
//                                                    .setNamespace("default")
//                                                    .setApp("my-service")
//                                                    .setDataWithVersion(dataWithVersion)
//                                                    .setResponseId("")
//                                                    .setKind(ConfigKind.FAULT_TOLERANCE_RULE.getKindName())
//                                                    .build();
//                                            responseObserver.onNext(response);
//                                            OpenSergoLogger.info("push config update");
//                                        } finally {
//                                            countDownLatch.countDown();
//                                        }
//                                    }
//                                });
//                                thread.start();
//                                try {
//                                    thread.join();
//                                } catch (InterruptedException e) {
//                                    e.printStackTrace();
//                                }
                            }

                            @Override
                            public void onError(Throwable throwable) {

                            }

                            @Override
                            public void onCompleted() {

                            }
                        };
                    }
                };
        serviceRegistry.addService(serviceImpl);

        // client call service
        SubscribeKey subscribeKey = new SubscribeKey("default", "my-service", ConfigKind.FAULT_TOLERANCE_RULE);
        client.subscribeConfig(subscribeKey, new OpenSergoConfigSubscriber() {
            @Override
            public boolean onConfigUpdate(SubscribeKey subscribeKey, Object data) {
                actualDatas[0] = data;
                OpenSergoLogger.info("config update key: {}, data: {}", subscribeKey, data);
//                countDownLatch.countDown();
                return true;
            }
        });

        countDownLatch.await();

        assertNotNull(actualRequests[0]);
        assertNotNull(actualRequests[0].getTarget());
        assertEquals(subscribeKey.getApp(), actualRequests[0].getTarget().getApp());
        assertEquals(subscribeKey.getNamespace(), actualRequests[0].getTarget().getNamespace());
        assertEquals(subscribeKey.getKind().getKindName(), actualRequests[0].getTarget().getKinds(0));

        assertNotNull(actualDatas[0]);
        assertTrue(actualDatas[0] instanceof List);
        List<Object> dataList = (List<Object>) actualDatas[0];
        assertNotNull(dataList.get(0));
        assertEquals(1, dataList.size());
        assertTrue(dataList.get(0) instanceof FaultToleranceRule);
        FaultToleranceRule faultToleranceRule = (FaultToleranceRule) dataList.get(0);
        assertNotNull(faultToleranceRule.getAction());
        assertEquals(actionRef.getKind(), faultToleranceRule.getAction().getKind());
        assertEquals(actionRef.getName(), faultToleranceRule.getAction().getName());

        assertNotNull(faultToleranceRule.getStrategiesList());
        assertEquals(1, faultToleranceRule.getStrategiesList().size());
        assertEquals(strategyRef.getKind(), faultToleranceRule.getStrategiesList().get(0).getKind());
        assertEquals(strategyRef.getName(), faultToleranceRule.getStrategiesList().get(0).getName());

        assertNotNull(faultToleranceRule.getTargetsList());
        assertEquals(1, faultToleranceRule.getTargetsList().size());
        assertEquals(ruleTargetRef.getTargetResourceName(), faultToleranceRule.getTargetsList().get(0).getTargetResourceName());
    }
}
