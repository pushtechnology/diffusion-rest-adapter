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

package com.pushtechnology.adapters.rest.metrics.event.listeners;

import com.pushtechnology.adapters.rest.metrics.TopicCreationFailedEvent;
import com.pushtechnology.adapters.rest.metrics.TopicCreationRequestEvent;
import com.pushtechnology.adapters.rest.metrics.TopicCreationSuccessEvent;

/**
 * Listener for events about topic creation.
 *
 * @author Push Technology Limited
 */
public interface TopicCreationEventListener {
    /**
     * Notified when an attempt to create a Diffusion topic is made.
     *
     * @param event the event
     */
    void onTopicCreationRequest(TopicCreationRequestEvent event);

    /**
     * Notified when a Diffusion topic is created.
     *
     * @param event the event
     */
    void onTopicCreationSuccess(TopicCreationSuccessEvent event);

    /**
     * Notified when a Diffusion topic cannot be created.
     *
     * @param event the event
     */
    void onTopicCreationFailed(TopicCreationFailedEvent event);
}
