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

package com.pushtechnology.adapters.rest.adapter;

import static java.util.stream.Collectors.toList;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;

import javax.net.ssl.SSLContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pushtechnology.adapters.rest.metrics.event.listeners.ServiceEventListener;
import com.pushtechnology.adapters.rest.model.latest.DiffusionConfig;
import com.pushtechnology.adapters.rest.model.latest.MetricsConfig;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.polling.EndpointClientImpl;
import com.pushtechnology.adapters.rest.polling.HttpClientFactory;
import com.pushtechnology.adapters.rest.publication.PublishingClientImpl;
import com.pushtechnology.adapters.rest.services.ServiceSessionFactoryImpl;
import com.pushtechnology.adapters.rest.session.management.DiffusionSessionFactory;
import com.pushtechnology.adapters.rest.session.management.EventedSessionListener;
import com.pushtechnology.adapters.rest.session.management.SSLContextFactory;
import com.pushtechnology.adapters.rest.session.management.SessionLossHandler;
import com.pushtechnology.adapters.rest.session.management.SessionLostListener;
import com.pushtechnology.adapters.rest.topic.management.TopicManagementClientImpl;
import com.pushtechnology.diffusion.client.session.Session;
import com.pushtechnology.diffusion.client.session.SessionFactory;

import net.jcip.annotations.GuardedBy;

/**
 * The REST Adapter.
 *
 * @author Push Technology Limited
 */
public final class InternalRESTAdapter implements RESTAdapterListener, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(InternalRESTAdapter.class);
    private final ScheduledExecutorService executor;
    private final SessionLossHandler sessionLossHandler;
    private final SSLContextFactory sslContextFactory;

    private final MetricsProviderFactory metricsProviderFactory = new MetricsProviderFactory();
    private final MetricsDispatcher metricsDispatcher = new MetricsDispatcher();
    private final EventedSessionListener eventedSessionListener = new EventedSessionListener();
    private final HttpClientFactory httpClientFactory;
    private final ServiceManager serviceManager = new ServiceManager();
    private final DiffusionSessionFactory sessionFactory;
    private final Runnable shutdownHandler;
    @GuardedBy("this")
    private Model currentModel;
    @GuardedBy("this")
    private EndpointClientImpl endpointClient;
    @GuardedBy("this")
    private SSLContext sslContext;
    @GuardedBy("this")
    private TopicManagementClientImpl topicManagementClient;
    @GuardedBy("this")
    private PublishingClientImpl publishingClient;
    @GuardedBy("this")
    private State state = State.INIT;
    @GuardedBy("this")
    private Session diffusionSession;
    @GuardedBy("this")
    private MetricsProvider metricsProvider;

    /**
     * Constructor.
     */
    // CHECKSTYLE.OFF: ParameterNumber
    public InternalRESTAdapter(
        Path relativePath,
        ScheduledExecutorService executor,
        SessionFactory sessions,
        HttpClientFactory httpClientFactory,
        ServiceEventListener serviceListener,
        SessionLossHandler sessionLossHandler,
        Runnable shutdownHandler,
        Session.Listener listener) {
    // CHECKSTYLE.ON: ParameterNumber

        this.executor = new ReportingScheduledExecutorService(executor);
        this.sessionLossHandler = sessionLossHandler;
        sslContextFactory = new SSLContextFactory(relativePath);
        this.httpClientFactory = httpClientFactory;
        sessionFactory = new DiffusionSessionFactory(sessions);
        this.shutdownHandler = shutdownHandler;
        eventedSessionListener.onSessionStateChange(listener);
        metricsDispatcher.addServiceEventListener(serviceListener);
    }

    // CHECKSTYLE.OFF: CyclomaticComplexity
    @Override
    public synchronized void onReconfiguration(Model model) {
        LOG.warn("Model {}", model);

        if (!model.isActive()) {
            shutdownSession();

            if (state == State.CONNECTING_TO_DIFFUSION) {
                state = State.STOPPING;
            }
            else {
                state = State.STOPPED;
                shutdownHandler.run();
            }
        }
        else if (state == State.INIT) {
            currentModel = model;
            reconfigureMetricsReporting();
            connectSession(model);
        }
        else if (isNotPolling(model)) {
            if (haveMetricsChanged(model)) {
                reconfigureMetricsReporting();
            }

            currentModel = model;
            shutdownPolling();
            state = State.STANDBY;
        }
        else if (hasTruststoreChanged(model) || hasDiffusionChanged(model)) {
            if (haveMetricsChanged(model)) {
                reconfigureMetricsReporting();
            }

            currentModel = model;
            shutdownSession();
            connectSession(model);
        }
        else if (state == State.STANDBY ||
                state == State.ACTIVE && (hasServiceSecurityChanged(model) || haveServicesChanged(model))) {
            if (haveMetricsChanged(model)) {
                reconfigureMetricsReporting();
            }

            currentModel = model;
            state = State.ACTIVE;
            reconfigureServiceManager();
        }
        else if (haveMetricsChanged(model)) {
            currentModel = model;
            reconfigureMetricsReporting();
        }
        else {
            currentModel = model;
        }
    }
    // CHECKSTYLE.ON: CyclomaticComplexity

    private void connectSession(Model model) {
        state = State.CONNECTING_TO_DIFFUSION;

        sslContext = sslContextFactory.create(model);
        sessionFactory
            .openSessionAsync(
                model.getDiffusion(),
                new SessionLostListener(sessionLossHandler),
                eventedSessionListener,
                sslContext)
            .thenAccept(this::onSessionOpen);
    }

    private void shutdownPolling() {
        if (state == State.ACTIVE) {
            serviceManager.close();
            endpointClient.close();
        }
    }

    private void shutdownSession() {
        shutdownPolling();

        if (state == State.ACTIVE || state == State.STANDBY) {
            diffusionSession.close();
        }
    }

    @Override
    public synchronized void onSessionOpen(Session session) {
        if (state == State.STOPPING) {
            state = State.STOPPED;

            session.close();
            shutdownHandler.run();
        }
        else if (isNotPolling(currentModel)) {
            diffusionSession = session;
            topicManagementClient = new TopicManagementClientImpl(
                metricsDispatcher,
                diffusionSession);
            publishingClient = new PublishingClientImpl(
                diffusionSession,
                eventedSessionListener,
                metricsDispatcher);
            state = State.STANDBY;
        }
        else {
            diffusionSession = session;
            topicManagementClient = new TopicManagementClientImpl(
                metricsDispatcher,
                diffusionSession);
            publishingClient = new PublishingClientImpl(
                diffusionSession,
                eventedSessionListener,
                metricsDispatcher);
            reconfigureServiceManager();
            state = State.ACTIVE;
        }
    }

    private void reconfigureServiceManager() {
        endpointClient = new EndpointClientImpl(
            currentModel,
            sslContext,
            httpClientFactory,
            metricsDispatcher);
        final ServiceSessionFactoryImpl serviceSessionFactory = new ServiceSessionFactoryImpl(
            executor,
            endpointClient,
            new EndpointPollHandlerFactoryImpl(publishingClient),
            topicManagementClient,
            publishingClient,
            metricsDispatcher);
        final ServiceManagerContext serviceManagerContext = new ServiceManagerContext(
            publishingClient,
            serviceSessionFactory);

        endpointClient.start();
        serviceManager.reconfigure(serviceManagerContext, currentModel);
    }

    private void reconfigureMetricsReporting() {
        LOG.debug("Updating metrics providers");

        if (metricsProvider != null) {
            metricsProvider.close();
        }
        metricsProvider = metricsProviderFactory.create(
            currentModel,
            executor,
            metricsDispatcher);
        metricsProvider.start();
    }

    @Override
    public synchronized void onSessionLost(Session session) {
        shutdownSession();
        state = State.RECOVERING;
    }

    @Override
    public synchronized void onSessionClosed(Session session) {
        diffusionSession = null;
        state = State.RECOVERING;
    }

    @GuardedBy("this")
    private synchronized boolean isNotPolling(Model model) {
        final DiffusionConfig diffusionConfig = model.getDiffusion();
        final List<ServiceConfig> services = model.getServices();

        return diffusionConfig == null ||
            services == null ||
            services.isEmpty() ||
            services.stream().map(ServiceConfig::getEndpoints).mapToInt(Collection::size).sum() == 0;
    }

    @GuardedBy("this")
    private boolean hasTruststoreChanged(Model newModel) {
        return currentModel.getTruststore() == null && newModel.getTruststore() != null ||
            currentModel.getTruststore() != null && !currentModel.getTruststore().equals(newModel.getTruststore());
    }

    private boolean hasServiceSecurityChanged(Model newModel) {
        return !currentModel
            .getServices()
            .stream()
            .map(ServiceConfig::getSecurity)
            .filter(Objects::nonNull)
            .filter(securityConfig -> securityConfig.getBasic() != null)
            .collect(toList())
            .equals(newModel
                .getServices()
                .stream()
                .map(ServiceConfig::getSecurity)
                .filter(Objects::nonNull)
                .filter(securityConfig -> securityConfig.getBasic() != null)
                .collect(toList()));
    }

    @GuardedBy("this")
    private boolean hasDiffusionChanged(Model model) {
        final DiffusionConfig diffusionConfig = model.getDiffusion();

        return !currentModel.getDiffusion().equals(diffusionConfig);
    }

    private boolean haveServicesChanged(Model model) {
        final List<ServiceConfig> newServices = model.getServices();
        final List<ServiceConfig> oldServices = currentModel.getServices();

        return !oldServices.equals(newServices);
    }

    private boolean haveMetricsChanged(Model model) {
        final MetricsConfig newMetrics = model.getMetrics();
        final MetricsConfig oldMetrics = currentModel.getMetrics();

        return !oldMetrics.equals(newMetrics);
    }

    @Override
    public void close() {
        if (state == State.STOPPED || state == State.STOPPING) {
            return;
        }

        if (state == State.ACTIVE) {
            serviceManager.release();
            endpointClient.close();
        }

        if (state == State.ACTIVE || state == State.STANDBY) {
            if (metricsProvider != null) {
                metricsProvider.close();
            }
            diffusionSession.close();
        }

        if (state == State.CONNECTING_TO_DIFFUSION) {
            state = State.STOPPING;
        }
        else {
            state = State.STOPPED;
            shutdownHandler.run();
        }
    }

    private enum State {
        // The adapter has been created but not yet configured
        INIT,
        // The adapter is connecting to the Diffusion server
        CONNECTING_TO_DIFFUSION,
        // The adapter has connected to Diffusion but has no endpoints to poll
        STANDBY,
        // The adapter has connected to Diffusion and is polling endpoints
        ACTIVE,
        // The adapter has lost it's connection to Diffusion and is recovering
        RECOVERING,
        // The adapter has started to shutdown
        STOPPING,
        // The adapter has finished shutting down
        STOPPED
    }
}
