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

package com.pushtechnology.adapters.rest.model.v12;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;
import lombok.Value;

/**
 * Endpoint configuration. Version 12.
 * <p>
 * Description of a REST endpoint to poll.
 *
 * @author Push Technology Limited
 */
@Value
@Builder
@AllArgsConstructor
@ToString(of = "name")
public class EndpointConfig {
    /**
     * The name of the endpoint.
     */
    String name;
    /**
     * The URL of the endpoint.
     */
    String url;
    /**
     * The topic to map the endpoint to.
     */
    String topic;
    /**
     * The type of content produced by the endpoint.
     */
    String produces;
}
