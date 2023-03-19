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

import io.grpc.internal.GrpcUtil;
import io.grpc.internal.ManagedChannelImplBuilder;
import io.grpc.netty.NettyChannelBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * @author Jiangnan Jia
 * @author Eric Zhao
 * @author Jax4Li
 */
public class OpenSergoClientConfig {
    private boolean useTransportSecurity;
    private int maxInboundMessageSize;
    private int maxRetryAttempts;
    private int maxHedgedAttempts;
    private long retryBufferSize;
    private long perRpcBufferLimit;
    private long idleTimeoutMillis;
    private long keepAliveTimeMillis;
    private long keepAliveTimeoutMillis;

    private File trustCertCollectionFile;
    private File keyCertChainFile;
    private File keyFile;
    private String keyPassword;

    public boolean isUseTransportSecurity() {
        return useTransportSecurity;
    }

    public int getMaxInboundMessageSize() {
        return maxInboundMessageSize;
    }

    public int getMaxRetryAttempts() {
        return maxRetryAttempts;
    }

    public int getMaxHedgedAttempts() {
        return maxHedgedAttempts;
    }

    public long getRetryBufferSize() {
        return retryBufferSize;
    }

    public long getPerRpcBufferLimit() {
        return perRpcBufferLimit;
    }

    public long getIdleTimeoutMillis() {
        return idleTimeoutMillis;
    }

    public long getKeepAliveTimeMillis() {
        return keepAliveTimeMillis;
    }

    public long getKeepAliveTimeoutMillis() {
        return keepAliveTimeoutMillis;
    }

    public File getTrustCertCollectionFile() {
        return trustCertCollectionFile;
    }

    public File getKeyCertChainFile() {
        return keyCertChainFile;
    }

    public File getKeyFile() {
        return keyFile;
    }

    public String getKeyPassword() {
        return keyPassword;
    }

    public static class Builder {
        private boolean useTransportSecurity;
        private File trustCertCollectionFile;
        private File keyCertChainFile;
        private File keyFile;
        private String keyPassword;

        /** @see io.grpc.internal.AbstractManagedChannelImplBuilder#maxInboundMessageSize */
        private int maxInboundMessageSize = GrpcUtil.DEFAULT_MAX_MESSAGE_SIZE;
        /** @see io.grpc.internal.ManagedChannelImplBuilder#maxRetryAttempts */
        private int maxRetryAttempts = 5;
        /** @see ManagedChannelImplBuilder#maxHedgedAttempts */
        private int maxHedgedAttempts = 5;
        /** @see ManagedChannelImplBuilder#retryBufferSize */
        private long retryBufferSize = 1L << 24;
        /** @see ManagedChannelImplBuilder#perRpcBufferLimit */
        private long perRpcBufferLimit = 1L << 20;
        /** @see ManagedChannelImplBuilder#IDLE_MODE_DEFAULT_TIMEOUT_MILLIS */
        private long idleTimeoutMillis = TimeUnit.MINUTES.toMillis(30);
        /** @see GrpcUtil#DEFAULT_KEEPALIVE_TIMEOUT_NANOS */
        private long keepAliveTimeoutMillis = TimeUnit.SECONDS.toMillis(20);
        /** @see NettyChannelBuilder#keepAliveTimeNanos */
        private long keepAliveTimeMillis = TimeUnit.NANOSECONDS.toMillis(GrpcUtil.KEEPALIVE_TIME_NANOS_DISABLED);

        public OpenSergoClientConfig.Builder serverTls(@Nonnull File trustCertCollectionFile) {
            this.useTransportSecurity = true;
            this.trustCertCollectionFile = trustCertCollectionFile;
            return this;
        }

        public OpenSergoClientConfig.Builder mutualTls(@Nonnull File keyCertChainFile, @Nonnull File keyFile, @Nullable String keyPassword) {
            this.useTransportSecurity = true;
            this.keyCertChainFile = keyCertChainFile;
            this.keyFile = keyFile;
            this.keyPassword = keyPassword;
            return this;
        }

        public OpenSergoClientConfig.Builder maxInboundMessageSize(int maxInboundMessageSize) {
            this.maxInboundMessageSize = maxInboundMessageSize;
            return this;
        }

        public OpenSergoClientConfig.Builder maxRetryAttempts(int maxRetryAttempts) {
            this.maxRetryAttempts = maxRetryAttempts;
            return this;
        }

        public OpenSergoClientConfig.Builder maxHedgedAttempts(int maxHedgedAttempts) {
            this.maxHedgedAttempts = maxHedgedAttempts;
            return this;
        }

        public OpenSergoClientConfig.Builder retryBufferSize(long retryBufferSize) {
            this.retryBufferSize = retryBufferSize;
            return this;
        }

        public OpenSergoClientConfig.Builder perRpcBufferLimit(long perRpcBufferLimit) {
            this.perRpcBufferLimit = perRpcBufferLimit;
            return this;
        }

        public OpenSergoClientConfig.Builder idleTimeoutMillis(long idleTimeoutMillis) {
            this.idleTimeoutMillis = idleTimeoutMillis;
            return this;
        }

        public OpenSergoClientConfig.Builder keepAliveTimeMillis(long keepAliveTimeMillis) {
            this.keepAliveTimeMillis = keepAliveTimeMillis;
            return this;
        }

        public OpenSergoClientConfig.Builder keepAliveTimeoutMillis(long keepAliveTimeoutMillis) {
            this.keepAliveTimeoutMillis = keepAliveTimeoutMillis;
            return this;
        }

        public OpenSergoClientConfig build() {
            OpenSergoClientConfig clientConfig =  new OpenSergoClientConfig();
            clientConfig.useTransportSecurity = this.useTransportSecurity;
            clientConfig.maxInboundMessageSize = this.maxInboundMessageSize;
            clientConfig.maxRetryAttempts = this.maxRetryAttempts;
            clientConfig.maxHedgedAttempts = this.maxHedgedAttempts;
            clientConfig.retryBufferSize = this.retryBufferSize;
            clientConfig.perRpcBufferLimit = this.perRpcBufferLimit;
            clientConfig.idleTimeoutMillis = this.idleTimeoutMillis;
            clientConfig.keepAliveTimeMillis = this.keepAliveTimeMillis;
            clientConfig.keepAliveTimeoutMillis = this.keepAliveTimeoutMillis;
            clientConfig.trustCertCollectionFile = this.trustCertCollectionFile;
            clientConfig.keyCertChainFile = this.keyCertChainFile;
            clientConfig.keyFile = this.keyFile;
            clientConfig.keyPassword = this.keyPassword;
            return new OpenSergoClientConfig();
        }
    }
}
