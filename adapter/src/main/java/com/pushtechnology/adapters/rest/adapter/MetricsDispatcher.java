/*******************************************************************************
 * Copyright (C) 2017 Push Technology Ltd.
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

package com.pushtechnology.adapters.rest.adapter;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.http.HttpResponse;

import com.pushtechnology.adapters.rest.metrics.listeners.PollListener;
import com.pushtechnology.adapters.rest.metrics.listeners.PublicationListener;
import com.pushtechnology.adapters.rest.metrics.listeners.TopicCreationListener;
import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.diffusion.client.callbacks.ErrorReason;
import com.pushtechnology.diffusion.client.features.control.topics.TopicAddFailReason;
import com.pushtechnology.diffusion.datatype.Bytes;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

/**
 * Dispatcher for metrics events.
 *
 * @author Matt Champion 05/07/2017
 */
@ThreadSafe
public final class MetricsDispatcher implements
        PollListener,
        PublicationListener,
        TopicCreationListener {

    @GuardedBy("this")
    private final Collection<PollListener> pollListeners;
    @GuardedBy("this")
    private final Collection<PublicationListener> publicationListeners;
    @GuardedBy("this")
    private final Collection<TopicCreationListener> topicCreationListeners;

    /**
     * Constructor.
     */
    public MetricsDispatcher() {
        pollListeners = new ArrayList<>();
        publicationListeners = new ArrayList<>();
        topicCreationListeners = new ArrayList<>();
    }

    /**
     * Add a poll listener.
     */
    public synchronized void addPollListener(PollListener pollListener) {
        pollListeners.add(pollListener);
    }

    /**
     * Add a publication listener.
     */
    public synchronized void addPublicationListener(PublicationListener publicationListener) {
        publicationListeners.add(publicationListener);
    }

    /**
     * Add a topic creation listener.
     */
    public synchronized void addTopicCreationListener(TopicCreationListener topicCreationListener) {
        topicCreationListeners.add(topicCreationListener);
    }

    @Override
    public PollCompletionListener onPollRequest(ServiceConfig serviceConfig, EndpointConfig endpointConfig) {
        final Collection<PollCompletionListener> listeners = new ArrayList<>();
        synchronized (this) {
            pollListeners.forEach(pollListener -> {
                final PollCompletionListener completionListener =
                    pollListener.onPollRequest(serviceConfig, endpointConfig);
                listeners.add(completionListener);
            });
        }
        return new PollCompletionListener() {
            @Override
            public void onPollResponse(HttpResponse response) {
                listeners.forEach(listener -> listener.onPollResponse(response));
            }

            @Override
            public void onPollFailure(Exception exception) {
                listeners.forEach(listener -> listener.onPollFailure(exception));
            }
        };
    }

    @Override
    public TopicCreationCompletionListener onTopicCreationRequest(
        ServiceConfig serviceConfig,
        EndpointConfig endpointConfig) {

        final Collection<TopicCreationCompletionListener> listeners = new ArrayList<>();
        synchronized (this) {
            topicCreationListeners.forEach(topicCreationListener -> {
                final TopicCreationCompletionListener completionListener =
                    topicCreationListener.onTopicCreationRequest(serviceConfig, endpointConfig);
                listeners.add(completionListener);
            });
        }
        return new TopicCreationCompletionListener() {
            @Override
            public void onTopicCreated() {
                listeners.forEach(TopicCreationCompletionListener::onTopicCreated);
            }

            @Override
            public void onTopicCreationFailed(TopicAddFailReason reason) {
                listeners.forEach(listener -> listener.onTopicCreationFailed(reason));
            }
        };
    }

    @Override
    public TopicCreationCompletionListener onTopicCreationRequest(
        ServiceConfig serviceConfig,
        EndpointConfig endpointConfig,
        Bytes value) {

        final Collection<TopicCreationCompletionListener> listeners = new ArrayList<>();
        synchronized (this) {
            topicCreationListeners.forEach(topicCreationListener -> {
                final TopicCreationCompletionListener completionListener =
                    topicCreationListener.onTopicCreationRequest(serviceConfig, endpointConfig, value);
                listeners.add(completionListener);
            });
        }
        return new TopicCreationCompletionListener() {
            @Override
            public void onTopicCreated() {
                listeners.forEach(TopicCreationCompletionListener::onTopicCreated);
            }

            @Override
            public void onTopicCreationFailed(TopicAddFailReason reason) {
                listeners.forEach(listener -> listener.onTopicCreationFailed(reason));
            }
        };
    }

    @Override
    public PublicationCompletionListener onPublicationRequest(
        ServiceConfig serviceConfig,
        EndpointConfig endpointConfig,
        Bytes value) {

        final Collection<PublicationCompletionListener> listeners = new ArrayList<>();
        synchronized (this) {
            publicationListeners.forEach(publicationListener -> {
                final PublicationCompletionListener completionListener =
                    publicationListener.onPublicationRequest(serviceConfig, endpointConfig, value);
                listeners.add(completionListener);
            });
        }
        return new PublicationCompletionListener() {
            @Override
            public void onPublication() {
                listeners.forEach(PublicationCompletionListener::onPublication);
            }

            @Override
            public void onPublicationFailed(ErrorReason reason) {
                listeners.forEach(listener -> listener.onPublicationFailed(reason));
            }
        };
    }
}