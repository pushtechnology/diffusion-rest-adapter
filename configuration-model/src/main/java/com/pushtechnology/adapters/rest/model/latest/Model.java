package com.pushtechnology.adapters.rest.model.latest;

import java.util.List;

import com.pushtechnology.adapters.rest.model.AnyModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

/**
 * Configuration model. Version 4.
 *
 * @author Push Technology Limited
 */
@Value
@Builder
@AllArgsConstructor
public class Model implements AnyModel {
    /**
     * The version of the model.
     */
    public static final int VERSION = 4;

    /**
     * The Diffusion server.
     */
    private Diffusion diffusion;

    /**
     * The REST services to poll.
     */
    List<Service> services;
}
