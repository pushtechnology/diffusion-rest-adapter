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

package com.pushtechnology.adapters.rest.model.latest;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

/**
 * Service configuration. Version 8.
 * <p>
 * Description of a REST service to poll.
 *
 * @author Push Technology Limited
 */
@Value
@Builder
@AllArgsConstructor
public class ServiceConfig {
    /**
     * The host of the service.
     */
    String host;

    /**
     * The port to connect to.
     */
    int port;

    /**
     * If a secure transport should be used.
     */
    boolean secure;

    /**
     * The endpoints the service makes available.
     */
    List<EndpointConfig> endpoints;

    /**
     * The time in milliseconds between polls.
     */
    long pollPeriod;

    /**
     * The topic root.
     */
    String topicRoot;
}
