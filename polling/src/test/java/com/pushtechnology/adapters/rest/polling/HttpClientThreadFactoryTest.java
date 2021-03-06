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

package com.pushtechnology.adapters.rest.polling;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit tests for {@link HttpClientThreadFactory}.
 *
 * @author Push Technology Limited
 */
public final class HttpClientThreadFactoryTest {
    @Test
    public void constructsDaemonThreads() {
        final HttpClientThreadFactory threadFactory = new HttpClientThreadFactory();

        final Thread thread = threadFactory.newThread(() -> { });

        assertNotNull(thread);
        assertEquals("HTTP client I/O 1", thread.getName());
        assertTrue(thread.isDaemon());
    }
}
