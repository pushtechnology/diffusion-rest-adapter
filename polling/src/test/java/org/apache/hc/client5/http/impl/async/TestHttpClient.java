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

package org.apache.hc.client5.http.impl.async;

import java.util.concurrent.Future;

import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.nio.AsyncPushConsumer;
import org.apache.hc.core5.http.nio.AsyncRequestProducer;
import org.apache.hc.core5.http.nio.AsyncResponseConsumer;
import org.apache.hc.core5.http.nio.HandlerFactory;
import org.apache.hc.core5.http.protocol.HttpContext;

/**
 * Mockable version of the HTTP client.
 *
 * @author Push Technology Limited
 */
public abstract class TestHttpClient extends CloseableHttpAsyncClient {
    @Override
    public abstract <T> Future<T> doExecute(
        HttpHost target,
        AsyncRequestProducer requestProducer,
        AsyncResponseConsumer<T> responseConsumer,
        HandlerFactory<AsyncPushConsumer> pushHandlerFactory,
        HttpContext context,
        FutureCallback<T> callback);
}
