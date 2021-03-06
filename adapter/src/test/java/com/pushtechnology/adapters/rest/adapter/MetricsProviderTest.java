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

package com.pushtechnology.adapters.rest.adapter;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

/**
 * Unit tests for {@link MetricsProvider}.
 *
 * @author Push Technology Limited
 */
public final class MetricsProviderTest {
    @Mock
    private Runnable startTask;
    @Mock
    private Runnable stopTask;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @After
    public void postConditions() {
        verifyNoMoreInteractions(startTask, stopTask);
    }

    @Test
    public void start() throws Exception {
        final MetricsProvider metricsProvider = new MetricsProvider(
            startTask,
            stopTask);

        metricsProvider.start();

        verify(startTask).run();
    }

    @Test
    public void close() throws Exception {
        final MetricsProvider metricsProvider = new MetricsProvider(
            startTask,
            stopTask);

        metricsProvider.close();

        verify(stopTask).run();
    }
}