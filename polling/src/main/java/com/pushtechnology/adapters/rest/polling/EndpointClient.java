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

import java.util.concurrent.CompletableFuture;

import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;

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
     * @return handle to asynchronous request
     * @throws IllegalStateException if the client is not running
     */
    CompletableFuture<EndpointResponse> request(
        ServiceConfig serviceConfig,
        EndpointConfig endpointConfig);

    /**
     * Start component.
     */
    void start();

    @Override
    void close();
}
