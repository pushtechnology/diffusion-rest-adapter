/*******************************************************************************
 * Copyright (C) 2021 Push Technology Ltd.
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

package com.pushtechnology.adapters.rest.topic.management;

import static java.lang.String.format;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import com.pushtechnology.adapters.rest.endpoints.EndpointType;
import com.pushtechnology.adapters.rest.metrics.listeners.TopicCreationListener;
import com.pushtechnology.adapters.rest.metrics.listeners.TopicCreationListener.TopicCreationCompletionListener;
import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.diffusion.client.features.control.topics.TopicAddFailReason;
import com.pushtechnology.diffusion.client.features.control.topics.TopicControl;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.client.topics.details.TopicSpecification;
import com.pushtechnology.diffusion.client.topics.details.TopicType;

/**
 * Topic management client to control Diffusion topic tree.
 *
 * @author Push Technology Limited
 */
public final class TopicManagementClientImpl implements TopicManagementClient {
    private final TopicCreationListener topicCreationListener;
    private final Session session;

    /**
     * Constructor.
     */
    public TopicManagementClientImpl(TopicCreationListener topicCreationListener, Session session) {
        this.topicCreationListener = topicCreationListener;
        this.session = session;
    }

    @Override
    public CompletableFuture<Void> addEndpoint(
            ServiceConfig serviceConfig,
            EndpointConfig endpointConfig) {

        final String produces = endpointConfig.getProduces();
        final String topicPath = serviceConfig.getTopicPathRoot() + "/" + endpointConfig.getTopicPath();
        final TopicType topicType = EndpointType.from(produces).getTopicType();

        final TopicSpecification specification = session
            .feature(TopicControl.class)
            .newSpecification(topicType)
            .withProperty(
                TopicSpecification.REMOVAL,
                format("when no session has \"$Principal eq '%s'\" for 1m", session.getPrincipal()));

        return addTopic(topicPath, specification);
    }

    @Override
    public void removeEndpoint(ServiceConfig serviceConfig, EndpointConfig endpointConfig) {
        session
            .feature(TopicControl.class)
            .removeTopics(serviceConfig.getTopicPathRoot() + "/" + endpointConfig.getTopicPath());
    }

    private CompletableFuture<Void> addTopic(String path, TopicSpecification specification) {
        final TopicCreationCompletionListener completionListener =
            topicCreationListener.onTopicCreationRequest(path, specification.getType());
        return session
            .feature(TopicControl.class)
            .addTopic(path, specification)
            .thenAccept(x -> completionListener.onTopicCreated())
            .whenComplete((x, t) -> {
                if (t instanceof CompletionException) {
                    final Throwable e = t.getCause();
                    if (e instanceof TopicControl.InvalidTopicPathException) {
                        completionListener.onTopicCreationFailed(TopicAddFailReason.INVALID_NAME);
                    }
                    else if (e instanceof TopicControl.IncompatibleExistingTopicException) {
                        completionListener.onTopicCreationFailed(TopicAddFailReason.EXISTS_INCOMPATIBLE);
                    }
                    else if (e instanceof TopicControl.TopicLicenseLimitException) {
                        completionListener.onTopicCreationFailed(TopicAddFailReason.EXCEEDED_LICENSE_LIMIT);
                    }
                    else if (e instanceof TopicControl.InvalidTopicSpecificationException) {
                        completionListener.onTopicCreationFailed(TopicAddFailReason.INVALID_DETAILS);
                    }
                    else {
                        completionListener.onTopicCreationFailed(TopicAddFailReason.UNEXPECTED_ERROR);
                    }
                }
                else if (t != null) {
                    completionListener.onTopicCreationFailed(TopicAddFailReason.UNEXPECTED_ERROR);
                }
            });
    }
}
