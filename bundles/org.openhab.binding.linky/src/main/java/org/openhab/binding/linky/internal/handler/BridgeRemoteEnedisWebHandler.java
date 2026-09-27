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

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.linky.internal.api.ApiConfig;
import org.openhab.binding.linky.internal.api.ApiConfigEnedisWeb;
import org.openhab.binding.linky.internal.config.LinkyBridgeWebConfiguration;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
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
 *
 */
@NonNullByDefault
public class BridgeRemoteEnedisWebHandler extends BridgeRemoteBaseHandler {
    private final Logger logger = LoggerFactory.getLogger(BridgeRemoteEnedisWebHandler.class);

    public BridgeRemoteEnedisWebHandler(Bridge bridge, final @Reference HttpClientFactory httpClientFactory,
            final @Reference OAuthFactory oAuthFactory, final @Reference HttpService httpService,
            ComponentContext componentContext, Gson gson) {
        super(bridge, httpClientFactory, oAuthFactory, httpService, componentContext, gson);
    }

    @Override
    public void initialize() {

        config = getConfigAs(LinkyBridgeWebConfiguration.class);
        if (!Objects.requireNonNull(config).seemsValid()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.config-error-mandatory-settings");
        }

        LinkyBridgeWebConfiguration webConfig = (LinkyBridgeWebConfiguration) config;
        enedisApi.getApiConfig().setUserName(webConfig.username);
        enedisApi.getApiConfig().setPassword(webConfig.password);
        enedisApi.getApiConfig().setInternalAuthId(webConfig.internalAuthId);

        super.initialize();
    }

    @Override
    public ApiConfig getApiConfig() {
        return new ApiConfigEnedisWeb();
    }

}
