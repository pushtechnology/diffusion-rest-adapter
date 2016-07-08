package com.pushtechnology.adapters.rest.model.conversion;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import com.pushtechnology.adapters.rest.model.AnyModel;
import com.pushtechnology.adapters.rest.model.v2.Model;
import com.pushtechnology.adapters.rest.model.v2.Service;

/**
 * Unit tests for {@link ConversionContext}.
 *
 * @author Push Technology Limited
 */
public final class ConversionContextTest {

    private ConversionContext converter;

    @Before
    public void setUp() {
        converter = ConversionContext
            .builder()
            .register(0, com.pushtechnology.adapters.rest.model.v0.Model.class, V0Converter.INSTANCE)
            .register(1, Model.class, V1Converter.INSTANCE)
            .register(2, Model.class, V2Converter.INSTANCE)
            .build();
    }

    @Test
    public void testConvertFromV1() {
        final Model model = converter.convert(Model.builder().services(Collections.<Service>emptyList()).build());

        assertEquals(0, model.getServices().size());
    }

    @Test
    public void testConvertFromV0() {
        final Model model = converter.convert(com.pushtechnology.adapters.rest.model.v0.Model.builder().build());

        assertEquals(0, model.getServices().size());
    }

    @Test
    public void testModelVersion0() {
        assertEquals(com.pushtechnology.adapters.rest.model.v0.Model.class, converter.modelVersion(0));
    }

    @Test
    public void testModelVersion1() {
        assertEquals(Model.class, converter.modelVersion(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUnknownModel() {
        converter.convert(new AnyModel() { });
    }
}
