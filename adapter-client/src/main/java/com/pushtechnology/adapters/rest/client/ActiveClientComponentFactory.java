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

package com.pushtechnology.adapters.rest.client;

import com.pushtechnology.adapters.rest.component.Component;
import com.pushtechnology.adapters.rest.model.latest.Model;
import com.pushtechnology.adapters.rest.polling.PollClient;

/**
 * Factory for active components for the {@link RESTAdapterClient}.
 *
 * @author Push Technology Limited
 */
public final class ActiveClientComponentFactory implements RESTAdapterComponentFactory {
    @Override
    public Component create(Model model, PollClient pollClient, RESTAdapterClientCloseHandle client) {
        final PublicationComponent publicationComponent = PublicationComponentImpl.create(model, client);
        final PollingComponent publishingComponent = publicationComponent.createPolling(model, pollClient);

        return () -> {
            publishingComponent.close();
            publicationComponent.close();
        };
    }
}