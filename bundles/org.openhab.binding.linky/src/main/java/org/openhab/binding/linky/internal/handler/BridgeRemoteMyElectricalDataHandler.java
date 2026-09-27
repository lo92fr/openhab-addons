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
import org.openhab.binding.linky.internal.api.ApiConfigMyElectrical;
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
 * {@link BridgeRemoteMyElectricalDataHandler} is the base handler to access enedis data.
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public class BridgeRemoteMyElectricalDataHandler extends BridgeRemoteApiHandler {
    private final Logger logger = LoggerFactory.getLogger(BridgeRemoteMyElectricalDataHandler.class);

    public static final String LINKY_MYELECTRICALDATA_CLIENT_ID = "";

    // https://www.myelectricaldata.fr/v1/oauth2/authorize?response_type=code&client_id=&state=linky&redirect_uri=http%3A%2F%2Flocalhost%3A8080%2Fconnectlinky&scope=am_application_scope+default&user_type=aa&person_id=-1&usage_points_id=aa

    public BridgeRemoteMyElectricalDataHandler(Bridge bridge, final @Reference HttpClientFactory httpClientFactory,
            final @Reference OAuthFactory oAuthFactory, final @Reference HttpService httpService,
            ComponentContext componentContext, Gson gson) {
        super(bridge, httpClientFactory, oAuthFactory, httpService, componentContext, gson);
    }

    @Override
    public boolean getIsSandbox() {
        return false;
    }

    @Override
    public void dispose() {
        logger.debug("Shutting down Netatmo API bridge handler.");

        super.dispose();
    }

    @Override
    public ApiConfig getApiConfig() {
        return new ApiConfigMyElectrical();
    }

}
