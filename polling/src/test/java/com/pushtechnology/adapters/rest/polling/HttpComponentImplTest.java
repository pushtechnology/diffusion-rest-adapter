package com.pushtechnology.adapters.rest.polling;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;
import java.util.concurrent.Future;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.pushtechnology.adapters.rest.model.latest.EndpointConfig;
import com.pushtechnology.adapters.rest.model.latest.ServiceConfig;
import com.pushtechnology.diffusion.datatype.json.JSON;

/**
 * Unit tests for {@link HttpComponentImpl}.
 *
 * @author Push Technology Limited
 */
public final class HttpComponentImplTest {
    @Mock
    private CloseableHttpAsyncClient httpClient;
    @Mock
    private FutureCallback<JSON> callback;
    @Mock
    private Future<HttpResponse> future;

    private final ServiceConfig serviceConfig = ServiceConfig.builder().host("localhost").port(8080).build();
    private final EndpointConfig endpointConfig = EndpointConfig.builder().url("/a/url.json").build();

    private HttpComponentImpl httpComponent;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        initMocks(this);

        when(httpClient.execute(isA(HttpHost.class), isA(HttpRequest.class), isA(FutureCallback.class)))
            .thenReturn(future);

        httpComponent = new HttpComponentImpl(httpClient);
    }

    @After
    public void postConditions() {
        verifyNoMoreInteractions(httpClient, callback);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void request() {
        final Future<?> handle = httpComponent.request(serviceConfig, endpointConfig, callback);

        assertEquals(future, handle);
        verify(httpClient).execute(isA(HttpHost.class), isA(HttpRequest.class), isA(FutureCallback.class));
    }

    @Test
    public void close() throws IOException {
        httpComponent.close();

        verify(httpClient).close();
    }
}