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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.http.ProtocolVersion;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHttpResponse;
import org.junit.Test;

/**
 * Unit tests for {@link EndpointResponseImpl}.
 *
 * @author Push Technology Limited
 */
public final class EndpointResponseImplTest {
    @Test
    public void getResponseLength() throws IOException {
        final EndpointResponse response = createResponse();
        assertEquals(13, response.getResponseLength());
    }

    @Test
    public void getResponse() throws IOException {
        final EndpointResponse response = createResponse();
        assertEquals("Hello, world!", new String(response.getResponse(), StandardCharsets.UTF_8));
        assertEquals("Hello, world!", new String(response.getResponse(), StandardCharsets.UTF_8));
    }

    @Test
    public void getHeader() throws IOException {
        final EndpointResponse response = createResponse();
        assertEquals("text/plain", response.getHeader("Content-Type"));
    }

    @Test
    public void getStatusCode() throws IOException {
        final EndpointResponse response = createResponse();
        assertEquals(200, response.getStatusCode());
    }

    private EndpointResponse createResponse() throws IOException {
        final ProtocolVersion protocolVersion = new ProtocolVersion("HTTP", 1, 1);
        final BasicHttpResponse httpResponse = new BasicHttpResponse(protocolVersion, 200, "OK");
        final BasicHttpEntity httpEntity = new BasicHttpEntity();
        httpEntity.setContent(new ByteArrayInputStream("Hello, world!".getBytes()));
        httpResponse.setEntity(httpEntity);
        httpResponse.addHeader("Content-Type", "text/plain");
        return EndpointResponseImpl.create(httpResponse);
    }
}
