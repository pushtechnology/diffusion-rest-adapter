/*******************************************************************************
 * Copyright (C) 2017 Push Technology Ltd.
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

package com.pushtechnology.adapters.rest.metrics;

import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.diffusion.client.callbacks.ErrorReason;
import com.pushtechnology.diffusion.datatype.Bytes;

/**
 * Listener for Diffusion topic publication events.
 *
 * @author Push Technology Limited
 */
public interface PublicationListener {
    /**
     * Notified when an attempt to update a Diffusion topic is made.
     *
     * @param serviceConfig the service
     * @param endpointConfig the endpoint
     * @param value the initial value
     * @return a listener for the completion of the publication request
     */
    PublicationCompletionListener onPublicationRequest(
        ServiceConfig serviceConfig,
        EndpointConfig endpointConfig,
        Bytes value);

    /**
     * Listener for the completion of a publication request.
     */
    interface PublicationCompletionListener {
        /**
         * Notified when a Diffusion topic is updated.
         *
         * @param value the initial value
         */
        void onPublication(Bytes value);

        /**
         * Notified when a Diffusion topic cannot be updated.
         *
         * @param value the initial value
         * @param reason the cause of failure
         */
        void onPublicationFailed(Bytes value, ErrorReason reason);

        /**
         * Implementation of {@link PublicationCompletionListener} that ignores notifications.
         */
        PublicationCompletionListener NULL_LISTENER = new PublicationCompletionListener() {
            @Override
            public void onPublication(Bytes value) {
            }

            @Override
            public void onPublicationFailed(Bytes value, ErrorReason reason) {
            }
        };
    }

    /**
     * Implementation of {@link PublicationListener} that ignores notifications.
     */
    PublicationListener NULL_LISTENER = (PublicationListener) (serviceConfig, endpointConfig, value) ->
        PublicationCompletionListener.NULL_LISTENER;
}