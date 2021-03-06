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

package com.pushtechnology.adapters.rest.metric.reporters;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.pushtechnology.adapters.rest.metrics.PollRequestEvent;
import com.pushtechnology.adapters.rest.metrics.PollSuccessEvent;

/**
 * Unit tests for {@link PollEventCounter}.
 *
 * @author Push Technology Limited
 */
public final class PollEventCounterTest {
    @Test
    public void onRequest() throws Exception {
        final PollEventCounter counter = new PollEventCounter();

        counter.onPollRequest(null);

        assertEquals(1, counter.getRequests());
        assertEquals(0, counter.getSuccesses());
        assertEquals(0, counter.getFailures());
    }

    @Test
    public void onSuccess() throws Exception {
        final PollEventCounter counter = new PollEventCounter();

        counter.onPollRequest(null);
        counter.onPollSuccess(PollSuccessEvent.Factory.create(PollRequestEvent.Factory.create(""), 200, 10));

        assertEquals(1, counter.getRequests());
        assertEquals(1, counter.getSuccesses());
        assertEquals(0, counter.getFailures());

        assertEquals(10, counter.getTotalPollResponseBytes());
    }

    @Test
    public void onFailure() throws Exception {
        final PollEventCounter counter = new PollEventCounter();

        counter.onPollRequest(null);
        counter.onPollFailed(null);

        assertEquals(1, counter.getRequests());
        assertEquals(0, counter.getSuccesses());
        assertEquals(1, counter.getFailures());
    }
}
