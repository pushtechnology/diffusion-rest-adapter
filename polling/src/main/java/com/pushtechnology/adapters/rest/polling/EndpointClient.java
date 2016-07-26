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

import java.io.IOException;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.http.concurrent.FutureCallback;

import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.diffusion.datatype.json.JSON;

/**
 * Client for requesting for endpoints.
 *
 * @author Push Technology Limited
 */
public interface EndpointClient extends AutoCloseable {
    /**
     * Poll an endpoint using the client.
     * @param serviceConfig the service
     * @param endpointConfig the endpoint
     * @param callback handler for the response
     * @return handle to asynchronous poll
     * @throws IllegalStateException if the client is not running
     */
    Future<?> request(ServiceConfig serviceConfig, EndpointConfig endpointConfig, FutureCallback<JSON> callback);

    /**
     * Start component.
     */
    @PostConstruct
    void start();

    @PreDestroy
    @Override
    void close() throws IOException;
}