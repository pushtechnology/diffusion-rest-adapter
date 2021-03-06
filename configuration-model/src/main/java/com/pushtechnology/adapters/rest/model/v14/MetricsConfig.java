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

package com.pushtechnology.adapters.rest.model.v14;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

/**
 * Metrics configuration. Version 14.
 * <p>
 * Description of the metrics to gather and report.
 *
 * @author Push Technology Limited
 */
@Value
@Builder
@AllArgsConstructor
public final class MetricsConfig {
    /**
     * If metrics are reported as a simple count of events.
     */
    boolean counting;
    /**
     * How metrics are reported as a summary of events.
     */
    SummaryConfig summary;
    /**
     * How metrics are reported through topics.
     */
    TopicConfig topic;
}
