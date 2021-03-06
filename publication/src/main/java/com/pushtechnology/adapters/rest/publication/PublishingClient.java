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

package com.pushtechnology.adapters.rest.publication;

import java.util.concurrent.CompletableFuture;

import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.diffusion.datatype.DataType;

/**
 * Publishing client to update Diffusion.
 *
 * @author Push Technology Limited
 */
public interface PublishingClient {
    /**
     * Add a service to publish to.
     */
    EventedUpdateSource addService(ServiceConfig serviceConfig);

    /**
     * Remove a service.
     */
    CompletableFuture<ServiceConfig> removeService(ServiceConfig serviceConfig);

    /**
     * Run a task if the service is registered with the client.
     */
    void forService(ServiceConfig serviceConfig, Runnable task);

    /**
     * Create an update context using an exclusive updater for the service.
     */
    <T> UpdateContext<T> createUpdateContext(
        ServiceConfig serviceConfig,
        EndpointConfig endpointConfig,
        Class<T> valueType,
        DataType<T> dataType);
}
