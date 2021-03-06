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

import java.util.concurrent.CompletableFuture;

import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;

/**
 * Topic management client for Diffusion server.
 *
 * @author Push Technology Limited
 */
public interface TopicManagementClient {
    /**
     * Start managing the topic for a REST endpoint.
     * @param serviceConfig the service of the endpoint
     * @param endpointConfig the endpoint
     * @return a future representing the completion of adding the endpoint topic
     */
    CompletableFuture<Void> addEndpoint(ServiceConfig serviceConfig, EndpointConfig endpointConfig);

    /**
     * Remove the topic for a REST endpoint.
     * @param serviceConfig the service of the endpoint
     * @param endpointConfig the endpoint
     */
    void removeEndpoint(ServiceConfig serviceConfig, EndpointConfig endpointConfig);
}
