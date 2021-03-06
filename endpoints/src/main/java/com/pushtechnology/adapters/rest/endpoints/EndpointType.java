/*******************************************************************************
 * Copyright (C) 2019 Push Technology Ltd.
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

package com.pushtechnology.adapters.rest.endpoints;

import static com.pushtechnology.diffusion.transform.transformer.Transformers.byteArrayToBinary;
import static java.util.Arrays.asList;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import com.pushtechnology.adapters.rest.polling.EndpointResponse;
import com.pushtechnology.diffusion.client.Diffusion;
import com.pushtechnology.diffusion.client.topics.details.TopicType;
import com.pushtechnology.diffusion.datatype.DataType;
import com.pushtechnology.diffusion.datatype.binary.Binary;
import com.pushtechnology.diffusion.datatype.json.JSON;
import com.pushtechnology.diffusion.transform.transformer.Transformers;
import com.pushtechnology.diffusion.transform.transformer.UnsafeTransformer;

/**
 * The supported endpoint types that can be processed by the adapter.
 *
 * @param <T> the type of value produced by the endpoint
 * @author Push Technology Limited
 */
public final class EndpointType<T> {
    /**
     * Endpoint type for JSON endpoints.
     */
    public static final EndpointType<JSON> JSON_ENDPOINT_TYPE = new EndpointType<>(
        asList("json", "application/json", "text/json"),
        TopicType.JSON,
        JSON.class,
        Diffusion.dataTypes().json(),
        Transformers
            .builder(EndpointResponse.class)
            .unsafeTransform(EndpointResponseToStringTransformer.INSTANCE)
            .unsafeTransform(Transformers.parseJSON())
            .buildUnsafe(),
        contentType ->
            contentType != null && (contentType.startsWith("application/json") || contentType.startsWith("text/json")));
    /**
     * Endpoint type for text endpoints.
     */
    public static final EndpointType<String> PLAIN_TEXT_ENDPOINT_TYPE = new EndpointType<>(
        asList("string", "text/plain"),
        TopicType.STRING,
        String.class,
        Diffusion.dataTypes().string(),
        Transformers
            .builder(EndpointResponse.class)
            .unsafeTransform(EndpointResponseToStringTransformer.INSTANCE)
            .buildUnsafe(),
        contentType ->
            contentType != null && (contentType.startsWith("text/plain") || JSON_ENDPOINT_TYPE.canHandle(contentType)));
    /**
     * Endpoint type for Binary endpoints.
     */
    public static final EndpointType<Binary> BINARY_ENDPOINT_TYPE = new EndpointType<>(
        asList("binary", "application/octet-stream"),
        TopicType.BINARY,
        Binary.class,
        Diffusion.dataTypes().binary(),
        Transformers
            .builder(EndpointResponse.class)
            .unsafeTransform(EndpointResponse::getResponse)
            .transform(byteArrayToBinary())
            .buildUnsafe(),
            contentType -> true);

    private static final Map<String, EndpointType> IDENTIFIER_LOOKUP = new HashMap<>();

    static {
        for (EndpointType<?> type : asList(JSON_ENDPOINT_TYPE, PLAIN_TEXT_ENDPOINT_TYPE, BINARY_ENDPOINT_TYPE)) {
            type.identifiers
                .stream()
                .map(identifier -> IDENTIFIER_LOOKUP.putIfAbsent(identifier, type))
                .filter(Objects::nonNull)
                .forEach(result -> {
                    throw new IllegalStateException(
                        "The EndpointType " + type + " has already been registered for an identifier");
                });
        }
    }

    private final Collection<String> identifiers;
    private final TopicType topicType;
    private final Class<T> valueType;
    private final DataType<T> dataType;
    private final UnsafeTransformer<EndpointResponse, T> parser;
    private final Predicate<String> canHandle;

    /**
     * Constructor.
     */
    private EndpointType(
        Collection<String> identifiers,
        TopicType topicType,
        Class<T> valueType,
        DataType<T> dataType,
        UnsafeTransformer<EndpointResponse, T> parser,
        Predicate<String> canHandle) {

        this.identifiers = identifiers;
        this.topicType = topicType;
        this.valueType = valueType;
        this.dataType = dataType;
        this.parser = parser;
        this.canHandle = canHandle;
    }

    /**
     * @return the topic type of the endpoint type
     */
    public TopicType getTopicType() {
        return topicType;
    }

    /**
     * @return the data type for the endpoint type
     */
    public DataType<T> getDataType() {
        return dataType;
    }

    /**
     * @return the value type for the endpoint type
     */
    public Class<T> getValueType() {
        return valueType;
    }

    /**
     * @return an identifier for the endpoint type
     */
    public String getIdentifier() {
        return identifiers.iterator().next();
    }

    /**
     * @return parser for endpoints
     */
    public UnsafeTransformer<EndpointResponse, T> getParser() {
        return parser;
    }

    /**
     * @return if the content type if valid for the endpoint type
     */
    public boolean canHandle(String contentType) {
        return canHandle.test(contentType);
    }

    /**
     * @return the endpoint type resolved from the name or a supported media type
     * @throws IllegalArgumentException if the endpoint type is unknown
     */
    public static EndpointType<?> from(String identifier) {
        final EndpointType endpointType = IDENTIFIER_LOOKUP.get(identifier);
        if (endpointType == null) {
            throw new IllegalArgumentException("Unknown endpoint type " + identifier);
        }
        return endpointType;
    }

    /**
     * @return best guess for the topic type for the provided content type
     */
    public static EndpointType<?> inferFromContentType(String contentType) {
        if (JSON_ENDPOINT_TYPE.canHandle(contentType)) {
            return JSON_ENDPOINT_TYPE;
        }
        else if (PLAIN_TEXT_ENDPOINT_TYPE.canHandle(contentType)) {
            return PLAIN_TEXT_ENDPOINT_TYPE;
        }
        else {
            return BINARY_ENDPOINT_TYPE;
        }
    }
}
