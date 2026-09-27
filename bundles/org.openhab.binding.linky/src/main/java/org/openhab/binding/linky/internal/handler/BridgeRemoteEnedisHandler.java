/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.linky.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.linky.internal.api.ApiConfig;
import org.openhab.binding.linky.internal.api.ApiConfigEnedisApi;
import org.openhab.binding.linky.internal.config.LinkyBridgeApiConfiguration;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * {@link BridgeRemoteEnedisHandler} is the base handler to access enedis data.
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public class BridgeRemoteEnedisHandler extends BridgeRemoteApiHandler {
    private final Logger logger = LoggerFactory.getLogger(BridgeRemoteEnedisHandler.class);

    public BridgeRemoteEnedisHandler(Bridge bridge, final @Reference HttpClientFactory httpClientFactory,
            final @Reference OAuthFactory oAuthFactory, final @Reference HttpService httpService,
            ComponentContext componentContext, Gson gson) {
        super(bridge, httpClientFactory, oAuthFactory, httpService, componentContext, gson);
    }

    @Override
    public boolean getIsSandbox() {
        LinkyBridgeApiConfiguration lcConfig = (LinkyBridgeApiConfiguration) config;
        return (lcConfig != null) ? lcConfig.isSandbox : false;
    }

    @Override
    public void dispose() {
        logger.debug("Shutting down Enedis bridge handler.");

        super.dispose();
    }

    @Override
    public ApiConfig getApiConfig() {
        return new ApiConfigEnedisApi(getIsSandbox());
    }

}
