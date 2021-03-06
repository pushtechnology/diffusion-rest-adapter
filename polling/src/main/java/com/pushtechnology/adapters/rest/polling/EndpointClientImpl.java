/*******************************************************************************
 * Copyright (C) 2020 Push Technology Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package com.pushtechnology.adapters.rest.polling;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.adapters.rest.metrics.listeners.PollListener;
import com.pushtechnology.adapters.rest.metrics.listeners.PollListener.PollCompletionListener;
import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;

import net.jcip.annotations.ThreadSafe;

/**
 * Implementation of {@link EndpointClient}.
 *
 * @author Push Technology Limited
 */
@ThreadSafe
public final class EndpointClientImpl implements EndpointClient {
    private static final Logger LOG = LoggerFactory.getLogger(EndpointClientImpl.class);
    private final Model model;
    private final SSLContext sslContext;
    private final HttpClientFactory clientFactory;
    private final PollListener pollListener;
    private volatile CloseableHttpAsyncClient client;

    /**
     * Constructor.
     */
    public EndpointClientImpl(
            Model model,
            SSLContext sslContext,
            HttpClientFactory clientFactory,
            PollListener pollListener) {
        this.model = model;
        this.sslContext = sslContext;
        this.clientFactory = clientFactory;
        this.pollListener = pollListener;
    }

    @Override
    public CompletableFuture<EndpointResponse> request(
            ServiceConfig serviceConfig,
            EndpointConfig endpointConfig) {

        if (client == null) {
            throw new IllegalStateException("Client not running");
        }

        final PollCompletionListener completionListener = pollListener.onPollRequest(serviceConfig, endpointConfig);

        final CompletableFuture<EndpointResponse> result = new CompletableFuture<>();
        client.execute(
            new HttpHost(serviceConfig.getHost(), serviceConfig.getPort(), serviceConfig.isSecure() ? "https" : "http"),
            new HttpGet(endpointConfig.getUrl()),
            new FutureCallback<HttpResponse>() {
                @Override
                public void completed(HttpResponse httpResponse) {
                    final StatusLine statusLine = httpResponse.getStatusLine();
                    if (statusLine.getStatusCode() >= 400) {
                        result.completeExceptionally(new Exception("Received response " + statusLine));
                        return;
                    }

                    try {
                        final EndpointResponse response = EndpointResponseImpl.create(httpResponse);
                        completionListener.onPollResponse(response);
                        result.complete(response);
                    }
                    catch (IOException e) {
                        completionListener.onPollFailure(e);
                        result.completeExceptionally(e);
                    }
                }

                @Override
                public void failed(Exception e) {
                    completionListener.onPollFailure(e);
                    result.completeExceptionally(e);
                }

                @Override
                public void cancelled() {
                    result.cancel(false);
                }
            });
        return result;
    }

    @Override
    public void start() {
        LOG.debug("Opening endpoint client");
        final CloseableHttpAsyncClient newClient = clientFactory.create(model, sslContext);
        newClient.start();
        client = newClient;
        LOG.debug("Opened endpoint client");
    }

    @Override
    public void close() {
        LOG.debug("Closing endpoint client");
        try {
            client.close();
        }
        catch (IOException e) {
            // The implementation does not throw an IOException
            throw new IllegalStateException(e);
        }
        LOG.debug("Closed endpoint client");
    }
}
