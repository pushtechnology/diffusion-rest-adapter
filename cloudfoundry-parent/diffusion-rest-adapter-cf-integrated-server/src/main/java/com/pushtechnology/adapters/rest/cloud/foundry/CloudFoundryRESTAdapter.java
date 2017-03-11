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

package com.pushtechnology.adapters.rest.cloud.foundry;

import java.io.IOException;

import javax.naming.NamingException;
import javax.net.ssl.SSLContext;

import com.pushtechnology.adapters.rest.cloud.foundry.vcap.ReapptCredentials;
import com.pushtechnology.adapters.rest.cloud.foundry.vcap.VCAP;
import com.pushtechnology.adapters.rest.integrated.server.RESTAdapterIntegratedServer;
import com.pushtechnology.adapters.rest.model.latest.DiffusionConfig;
import com.pushtechnology.adapters.rest.session.management.SSLContextFactory;

/**
 * Entry point for Diffusion Cloud Foundry REST Adapter.
 *
 * @author Push Technology Limited
 */
public final class CloudFoundryRESTAdapter {
    private CloudFoundryRESTAdapter() {
    }

    /**
     * Entry point for Cloud Foundry REST Adapter.
     * @param args The command line arguments
     * @throws NamingException if there was a problem starting the integrated server
     * @throws IllegalStateException if there was a problem starting the integrated server
     */
    // CHECKSTYLE.OFF: UncommentedMain // Entry point for runnable JAR
    public static void main(String[] args) throws NamingException, IOException {
        // CHECKSTYLE.ON: UncommentedMain

        final ReapptCredentials reapptCredentials = VCAP.getServices()
            .getReappt()
            .getCredentials();

        final SSLContext sslContext = SSLContextFactory.loadFromResource("reapptTruststore.jks");

        RESTAdapterIntegratedServer
            .create(
                VCAP.getPort(),
                DiffusionConfig.builder()
                    .host(reapptCredentials.getHost())
                    .port(443)
                    .secure(true)
                    .principal(reapptCredentials.getPrincipal())
                    .password(reapptCredentials.getCredentials()),
                sslContext)
            .start();
    }
}