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

package com.pushtechnology.adapters.rest.adapter;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.polling.ServiceSession;
import com.pushtechnology.adapters.rest.polling.ServiceSessionFactory;
import com.pushtechnology.adapters.rest.publication.PublishingClient;
import com.pushtechnology.adapters.rest.topic.management.TopicManagementClient;

/**
 * Implementation for {@link ServiceSessionGroup}.
 *
 * @author Push Technology Limited
 */
public final class ServiceSessionGroupImpl implements ServiceSessionGroup {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceSessionGroupImpl.class);
    private final Model model;
    private final TopicManagementClient topicManagementClient;
    private final PublishingClient publishingClient;
    private final List<ServiceSession> serviceSessions;
    private final ServiceSessionFactory serviceSessionFactory;
    private final ServiceListener serviceListener;

    /**
     * Constructor.
     */
    public ServiceSessionGroupImpl(
            Model model,
            TopicManagementClient topicManagementClient,
            PublishingClient publishingClient,
            ServiceSessionFactory serviceSessionFactory,
            ServiceListener serviceListener) {
        this.model = model;
        this.topicManagementClient = topicManagementClient;
        this.publishingClient = publishingClient;
        this.serviceSessionFactory = serviceSessionFactory;
        this.serviceListener = serviceListener;
        this.serviceSessions = new ArrayList<>();
    }

    @PostConstruct
    @Override
    public synchronized void start() {
        LOG.info("Opening service session group");
        for (final ServiceConfig service : model.getServices()) {
            final ServiceSession serviceSession = serviceSessionFactory.create(service);
            topicManagementClient.addService(service);
            publishingClient
                .addService(service)
                .onStandby(() -> serviceListener.onStandby(service))
                .onActive(new ServiceReadyForPublishing(topicManagementClient, serviceSession, service)
                    .andThen((updater) -> serviceListener.onActive(service)))
                .onClose(() -> serviceListener.onRemove(service));
            serviceSessions.add(serviceSession);
        }
        LOG.info("Opened service session group");
    }

    @PreDestroy
    @Override
    public synchronized void close() {
        LOG.info("Closing service session group");
        serviceSessions.forEach(ServiceSession::stop);
        model.getServices().forEach(publishingClient::removeService);
        LOG.info("Closed service session group");
    }
}
