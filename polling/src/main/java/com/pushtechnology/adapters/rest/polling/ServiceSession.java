/*******************************************************************************
 * Copyright (C) 2016 Push Technology Ltd.
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

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import org.apache.http.concurrent.FutureCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.adapters.PublishingClient;
import com.pushtechnology.adapters.rest.model.latest.Endpoint;
import com.pushtechnology.adapters.rest.model.latest.Service;
import com.pushtechnology.diffusion.datatype.json.JSON;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

/**
 * The session for a REST service.
 * <p>
 * Supports multiple endpoints and dynamically changing the endpoints. Access to the endpoints is synchronised.
 *
 * @author Push Technology Limited
 */
@ThreadSafe
public final class ServiceSession {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceSession.class);
    @GuardedBy("this")
    private final Map<Endpoint, PollHandle> endpointPollers = new HashMap<>();
    private final ScheduledExecutorService executor;
    private final PollClient pollClient;
    private final Service service;
    private final PublishingClient diffusionClient;
    @GuardedBy("this")
    private boolean isRunning = false;

    /**
     * Constructor.
     */
    public ServiceSession(
            ScheduledExecutorService executor,
            PollClient pollClient,
            Service service,
            PublishingClient diffusionClient) {
        this.executor = executor;
        this.pollClient = pollClient;
        this.service = service;
        this.diffusionClient = diffusionClient;
    }

    /**
     * Start the session.
     */
    public synchronized void start() {
        isRunning = true;

        service.getEndpoints().forEach(this::startEndpoint);
    }

    /**
     * Start polling an endpoint.
     */
    public synchronized void startEndpoint(Endpoint endpoint) {
        assert !endpointPollers.containsKey(endpoint) : "The endpoint has already been started";
        final PollResultHandler handler = new PollResultHandler(endpoint);
        final ScheduledFuture<?> future = executor.scheduleWithFixedDelay(
            new PollingTask(endpoint, handler),
            0L,
            service.getPollPeriod(),
            MILLISECONDS);
        endpointPollers.put(endpoint, new PollHandle(future));
    }

    /**
     * Stop polling an endpoint.
     */
    public synchronized void stopEndpoint(Endpoint endpoint) {
        assert endpointPollers.containsKey(endpoint) : "The endpoint has not been started";
        final PollHandle pollHandle = endpointPollers.remove(endpoint);
        pollHandle.taskHandle.cancel(false);
        if (pollHandle.currentPollHandle != null) {
            pollHandle.currentPollHandle.cancel(false);
        }
    }

    /**
     * Start the session.
     */
    public synchronized void stop() {
        isRunning = false;
        service.getEndpoints().forEach(this::stopEndpoint);
    }

    /**
     * The polling task. Triggers an asynchronous poll request.
     */
    private final class PollingTask implements Runnable {
        private final Endpoint endpoint;
        private final PollResultHandler handler;

        public PollingTask(Endpoint endpoint, PollResultHandler handler) {
            this.endpoint = endpoint;
            this.handler = handler;
        }

        @Override
        public void run() {
            synchronized (ServiceSession.this) {
                endpointPollers.get(endpoint).currentPollHandle = pollClient.request(
                    service,
                    endpoint,
                    handler);
            }
        }
    }

    /**
     * The handler for the polling result. Notifies the publishing client of the new data.
     */
    private final class PollResultHandler implements FutureCallback<JSON> {
        private final Endpoint endpoint;

        private PollResultHandler(Endpoint endpoint) {
            this.endpoint = endpoint;
        }

        @Override
        public void completed(JSON json) {
            LOG.trace("Polled value {}", json.toJsonString());

            synchronized (ServiceSession.this) {
                if (isRunning) {
                    diffusionClient.publish(endpoint, json);
                }
            }
        }

        @Override
        public void failed(Exception e) {
            LOG.warn("Poll failed", e);
        }

        @Override
        public void cancelled() {
            LOG.warn("Poll cancelled");
        }
    }

    /**
     * Represent a poll. Holds a handle to the task triggering a poll and a handle to the outstanding poll.
     */
    private static final class PollHandle {
        private final Future<?> taskHandle;
        @GuardedBy("ServiceSession.this")
        private Future<?> currentPollHandle;

        private PollHandle(Future<?> taskHandle) {
            this.taskHandle = taskHandle;
            currentPollHandle = null;
        }
    }
}
