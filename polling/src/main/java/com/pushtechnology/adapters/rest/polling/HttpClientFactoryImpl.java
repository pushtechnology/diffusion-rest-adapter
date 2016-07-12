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

import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;

import net.jcip.annotations.Immutable;

/**
 * Implementation of {@link HttpClientFactory}s.
 *
 * @author Push Technology Limited
 */
@Immutable
public final class HttpClientFactoryImpl implements HttpClientFactory {
    @Override
    public CloseableHttpAsyncClient create() {
        return HttpAsyncClients.custom()
            .disableCookieManagement()
            .disableAuthCaching()
            .build();
    }
}
