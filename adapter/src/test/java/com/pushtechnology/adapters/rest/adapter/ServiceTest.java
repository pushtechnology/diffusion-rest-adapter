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

import static java.util.Collections.emptyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.adapters.rest.publication.PublishingClient;
import com.pushtechnology.adapters.rest.services.ServiceSession;
import com.pushtechnology.adapters.rest.services.ServiceSessionFactory;

/**
 * Unit tests for {@link Service}.
 *
 * @author Push Technology Limited
 */
public final class ServiceTest {

    @Mock
    private PublishingClient publishingClient;
    @Mock
    private ServiceSessionFactory serviceSessionFactory;
    @Mock
    private ServiceSession serviceSession;

    private ServiceConfig serviceConfig = ServiceConfig
        .builder()
        .name("service")
        .host("localhost")
        .endpoints(emptyList())
        .topicPathRoot("root")
        .build();

    private ServiceManagerContext context;

    @Before
    public void setUp() {
        initMocks(this);

        context = new ServiceManagerContext(publishingClient, serviceSessionFactory);

        when(serviceSessionFactory.create(serviceConfig)).thenReturn(serviceSession);
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(publishingClient, serviceSessionFactory, serviceSession);
    }

    @Test
    public void configure() {
        final Service service = new Service();

        service.reconfigure(serviceConfig, context);

        verify(serviceSessionFactory).create(serviceConfig);
    }

    @Test
    public void reconfigure() {
        final Service service = new Service();

        service.reconfigure(serviceConfig, context);

        verify(serviceSessionFactory).create(serviceConfig);

        service.reconfigure(serviceConfig, context);

        verify(publishingClient).removeService(serviceConfig);

        verify(serviceSession).stop();
        verify(serviceSessionFactory, times(2)).create(serviceConfig);
    }

    @Test
    public void close() {
        final Service service = new Service();

        service.close();
    }

    @Test
    public void configureClose() {
        final Service service = new Service();

        service.reconfigure(serviceConfig, context);

        verify(serviceSessionFactory).create(serviceConfig);

        service.close();

        verify(serviceSession).stop();
        verify(publishingClient).removeService(serviceConfig);
    }

    @Test
    public void configureRelease() {
        final Service service = new Service();

        service.reconfigure(serviceConfig, context);

        verify(serviceSessionFactory).create(serviceConfig);

        service.release();

        verify(serviceSession).release();
        verify(publishingClient).removeService(serviceConfig);
    }
}
