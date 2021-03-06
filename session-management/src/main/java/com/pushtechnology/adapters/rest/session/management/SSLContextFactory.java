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

package com.pushtechnology.adapters.rest.session.management;

import static java.security.KeyStore.getDefaultType;
import static javax.net.ssl.TrustManagerFactory.getDefaultAlgorithm;
import static javax.net.ssl.TrustManagerFactory.getInstance;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import com.pushtechnology.adapters.rest.model.latest.Model;

import net.jcip.annotations.Immutable;

/**
 * Factory for {@link SSLContext}.
 *
 * @author Push Technology Limited
 */
@Immutable
public final class SSLContextFactory {
    private final Path relativePath;

    /**
     * Constructor.
     */
    public SSLContextFactory(Path relativePath) {
        this.relativePath = relativePath;
    }

    /**
     * @return an {@link SSLContext}.
     */
    public SSLContext create(Model model) {
        final String truststoreLocation = model.getTruststore();

        return loadFromResource(relativePath, truststoreLocation);
    }

    /**
     * @return an {@link SSLContext} from a trust store
     */
    public static SSLContext loadFromResource(Path relativePath, String truststoreLocation) {
        if (truststoreLocation == null) {
            return null;
        }

        try (InputStream stream = resolveTruststore(relativePath, truststoreLocation)) {
            final KeyStore keyStore = KeyStore.getInstance(getDefaultType());
            keyStore.load(stream, null);
            final TrustManagerFactory trustManagerFactory = getInstance(getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
            return sslContext;
        }
        catch (KeyStoreException |
            CertificateException |
            NoSuchAlgorithmException |
            IOException |
            KeyManagementException e) {

            throw new IllegalArgumentException("An SSLContext could not be created from " + truststoreLocation, e);
        }
    }

    private static InputStream resolveTruststore(Path relativePath, String truststoreLocation) throws IOException {
        final InputStream stream = Thread
            .currentThread()
            .getContextClassLoader()
            .getResourceAsStream(truststoreLocation);

        if (stream == null) {
            return Files.newInputStream(relativePath.resolve(truststoreLocation));
        }
        return stream;
    }
}
