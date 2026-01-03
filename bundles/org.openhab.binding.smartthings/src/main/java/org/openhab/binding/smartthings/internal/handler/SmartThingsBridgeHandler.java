/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.smartthings.internal.handler;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import javax.ws.rs.client.ClientBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smartthings.internal.SmartThingsAccountHandler;
import org.openhab.binding.smartthings.internal.SmartThingsAuthService;
import org.openhab.binding.smartthings.internal.SmartThingsBindingConstants;
import org.openhab.binding.smartthings.internal.SmartThingsHandlerFactory;
import org.openhab.binding.smartthings.internal.SmartThingsServlet;
import org.openhab.binding.smartthings.internal.api.SmartThingsApi;
import org.openhab.binding.smartthings.internal.api.SmartThingsNetworkConnector;
import org.openhab.binding.smartthings.internal.api.SmartThingsNetworkConnectorImpl;
import org.openhab.binding.smartthings.internal.discovery.SmartThingsDiscoveryService;
import org.openhab.binding.smartthings.internal.type.SmartThingsException;
import org.openhab.binding.smartthings.internal.type.SmartThingsTypeRegistry;
import org.openhab.core.auth.client.oauth2.AccessTokenRefreshListener;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.osgi.service.jaxrs.client.SseEventSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.HttpServer;

/**
 * The {@link SmartThingsBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bob Raker - Initial contribution
 */
@NonNullByDefault
public abstract class SmartThingsBridgeHandler extends BaseBridgeHandler
        implements SmartThingsAccountHandler, AccessTokenRefreshListener {
    private final Logger logger = LoggerFactory.getLogger(SmartThingsBridgeHandler.class);

    protected SmartThingsBridgeConfig config;

    protected SmartThingsHandlerFactory smartthingsHandlerFactory;
    protected BundleContext bundleContext;
    private @NonNullByDefault({}) HttpService httpService;
    private @NonNullByDefault({}) HttpClientFactory httpClientFactory;
    private @NonNullByDefault({}) SmartThingsApi smartthingsApi;
    private @NonNullByDefault({}) SmartThingsAuthService authService;
    protected @NonNullByDefault({}) SmartThingsTypeRegistry typeRegistry;
    protected @NonNullByDefault({}) SmartThingsDiscoveryService discoService;
    protected @NonNullByDefault({}) ClientBuilder clientBuilder;
    protected @NonNullByDefault({}) SseEventSourceFactory eventSourceFactory;

    private @Nullable SmartThingsServlet servlet;
    private @Nullable OAuthClientService oAuthService;
    private @NonNullByDefault({}) SmartThingsNetworkConnector networkConnector;
    private final OAuthFactory oAuthFactory;
    private String appId = "";

    private @Nullable HttpServer callbackServer;

    public SmartThingsBridgeHandler(Bridge bridge, SmartThingsHandlerFactory smartthingsHandlerFactory,
            SmartThingsAuthService authService, BundleContext bundleContext, HttpService httpService,
            OAuthFactory oAuthFactory, HttpClientFactory httpClientFactory, SmartThingsTypeRegistry typeRegistry,
            ClientBuilder clientBuilder, SseEventSourceFactory eventSourceFactory) {
        super(bridge);

        this.smartthingsHandlerFactory = smartthingsHandlerFactory;
        this.bundleContext = bundleContext;
        this.httpService = httpService;
        this.oAuthFactory = oAuthFactory;
        this.authService = authService;
        this.httpClientFactory = httpClientFactory;
        this.typeRegistry = typeRegistry;
        this.clientBuilder = clientBuilder;
        this.eventSourceFactory = eventSourceFactory;

        config = getThing().getConfiguration().as(SmartThingsBridgeConfig.class);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Commands are handled by the "Things"
    }

    @Reference
    public void registerDiscoveryListener(SmartThingsDiscoveryService disco) {
        this.discoService = disco;
        this.discoService.setSmartThingsTypeRegistry(typeRegistry);
    }

    @Override
    public void initialize() {
        // Validate the config
        if (!validateConfig(this.config)) {
            return;
        }

        OAuthClientService oAuthService = oAuthFactory.createOAuthClientService(thing.getUID().getAsString(),
                SmartThingsBindingConstants.SMARTTHINGS_API_TOKEN_URL,
                SmartThingsBindingConstants.SMARTTHINGS_AUTHORIZE_URL, SmartThingsBindingConstants.CLI_CLIENT_ID, null,
                SmartThingsBindingConstants.SMARTTHINGS_SCOPES, true);

        this.oAuthService = oAuthService;
        oAuthService.addAccessTokenRefreshListener(SmartThingsBridgeHandler.this);
        authService.registerServlet();

        if (servlet == null) {
            SmartThingsBridgeHandler bridgeHandler = this;
            SmartThingsServlet servlet = new SmartThingsServlet(bridgeHandler, httpService, networkConnector);
            this.servlet = servlet;
            servlet.activate();
        }

        try {
            org.openhab.core.auth.client.oauth2.AccessTokenResponse response = oAuthService.getAccessTokenResponse();
            if (response != null && response.getAccessToken() != null) {
                setupClient();
                logger.info("token:" + response.getAccessToken());
            } else {
                startCallbackListener();

                org.openhab.core.auth.client.oauth2.OAuthClientService srv = oAuthService;
                String authUrl = srv != null ? srv.getAuthorizationUrl(SmartThingsBindingConstants.REDIRECT_URI,
                        SmartThingsBindingConstants.SMARTTHINGS_SCOPES, getThing().getUID().getId()) : "";

                String authUrl2 = srv != null ? srv.getAuthorizationUrl(SmartThingsBindingConstants.REDIRECT_URI,
                        SmartThingsBindingConstants.SMARTTHINGS_SCOPES, getThing().getUID().getId())
                        + "&client_type=USER_LEVEL" : "";

                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Please authorize the binding by visiting: " + authUrl
                                + "\nThe authorization code will be captured automatically.");
            }
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "OAuth failed: " + e.getMessage());
        }

        /*
         *
         *
         *
         * updateStatus(ThingStatus.ONLINE);
         */
    }

    private void startCallbackListener() {
        stopCallbackListener();
        try {
            HttpServer server = HttpServer.create(new java.net.InetSocketAddress(61973), 0);
            callbackServer = server;
            server.createContext("/finish", exchange -> {
                String query = exchange.getRequestURI().getQuery();
                if (query != null && query.contains("code=")) {
                    String code = query.split("code=")[1].split("&")[0];
                    logger.debug("Captured auth code: {}", code);

                    String response = "Authorization successful! You can now close this window.";
                    exchange.sendResponseHeaders(200, response.length());
                    java.io.OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();

                    // Finish OAuth flow
                    finishOAuth(code, getThing().getUID().getId());
                    stopCallbackListener();
                } else {
                    exchange.sendResponseHeaders(400, 0);
                    exchange.close();
                }
            });
            server.setExecutor(null);
            server.start();
            logger.info("Started OAuth callback listener on port 61973");
        } catch (java.io.IOException e) {
            logger.error("Failed to start OAuth callback listener", e);
        }
    }

    protected void setupClient() {
        final org.openhab.core.auth.client.oauth2.OAuthClientService oAuthService = this.oAuthService;

        if (oAuthService != null) {
            this.networkConnector = new SmartThingsNetworkConnectorImpl(httpClientFactory, oAuthService);
            smartthingsApi = new SmartThingsApi(httpClientFactory, this, networkConnector, clientBuilder,
                    eventSourceFactory);
        }

    }

    private void finishOAuth(String code, String verifier) {
        org.openhab.core.auth.client.oauth2.OAuthClientService srv = oAuthService;
        if (srv != null) {
            try {
                srv.addExtraAuthField("code_verifier", verifier);
                org.openhab.core.auth.client.oauth2.AccessTokenResponse response = srv
                        .getAccessTokenResponseByAuthorizationCode(code, SmartThingsBindingConstants.REDIRECT_URI);
                if (response.getAccessToken() != null) {
                    logger.info("token:" + response.getAccessToken());
                    setupClient();
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Failed to exchange code for tokens");
                }
            } catch (Exception e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Token exchange failed: " + e.getMessage());
            }
        }
    }

    private void stopCallbackListener() {
        HttpServer server = callbackServer;
        if (server != null) {
            server.stop(0);
            callbackServer = null;
        }
    }

    @Override
    public void onAccessTokenResponse(AccessTokenResponse tokenResponse) {
    }

    public SmartThingsApi getSmartThingsApi() {
        return smartthingsApi;
    }

    @Override
    public void dispose() {
        stopCallbackListener();
        SmartThingsServlet servlet = this.servlet;
        if (servlet != null) {
            servlet.deactivate();
        }
        super.dispose();
    }

    protected boolean validateConfig(SmartThingsBridgeConfig config) {
        return true;
    }

    public SmartThingsHandlerFactory getSmartThingsHandlerFactory() {
        return smartthingsHandlerFactory;
    }

    public BundleContext getBundleContext() {
        return bundleContext;
    }

    @Override
    public boolean isAuthorized() {
        final AccessTokenResponse accessTokenResponse = getAccessTokenResponse();

        return accessTokenResponse != null && accessTokenResponse.getAccessToken() != null
                && accessTokenResponse.getRefreshToken() != null;
    }

    public @Nullable AccessTokenResponse getAccessTokenResponse() {
        try {
            OAuthClientService oAuthService = this.oAuthService;
            return oAuthService == null ? null : oAuthService.getAccessTokenResponse();
        } catch (OAuthException | IOException | OAuthResponseException | RuntimeException e) {
            logger.debug("Exception checking authorization: ", e);
            return null;
        }
    }

    public @Nullable AccessTokenResponse refreshToken() {
        try {
            OAuthClientService oAuthService = this.oAuthService;
            return oAuthService == null ? null : oAuthService.refreshToken();
        } catch (OAuthException | IOException | OAuthResponseException | RuntimeException e) {
            logger.debug("Exception checking authorization: ", e);
            return null;
        }
    }

    public @Nullable AccessTokenResponse getAccessTokenByClientCredentials() {
        try {
            OAuthClientService oAuthService = this.oAuthService;
            return oAuthService == null ? null
                    : oAuthService.getAccessTokenByClientCredentials(SmartThingsBindingConstants.SMARTTHINGS_SCOPES);
        } catch (OAuthException | IOException | OAuthResponseException | RuntimeException e) {
            logger.debug("Exception checking authorization: ", e);
            return null;
        }
    }

    @Override
    public String authorize(String redirectUri, String reqCode) throws SmartThingsException {
        try {
            OAuthClientService oAuthService = this.oAuthService;
            if (oAuthService == null) {
                throw new OAuthException("OAuth service is not initialized");
            }
            logger.debug("Make call to SmartThings to get access token.");
            oAuthService.getAccessTokenResponseByAuthorizationCode(reqCode, redirectUri);
            return reqCode;
        } catch (RuntimeException | OAuthException | IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            throw new SmartThingsException("unable to authorize request", e);
        } catch (final OAuthResponseException e) {
            throw new SmartThingsException("unable to authorize request", e);
        }
    }

    @Override
    public String formatAuthorizationUrl(String redirectUri, String state) {
        try {
            OAuthClientService oAuthService = this.oAuthService;
            if (oAuthService == null) {
                throw new OAuthException("OAuth service is not initialized");
            }

            return oAuthService.getAuthorizationUrl(redirectUri, null, state);
        } catch (final OAuthException e) {
            logger.debug("Error constructing AuthorizationUrl: ", e);
            return "";
        }
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppId() {
        return this.appId;
    }

    public SmartThingsNetworkConnector getNetworkConnector() {
        return this.networkConnector;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(SmartThingsDiscoveryService.class);
    }

    public SmartThingsTypeRegistry getSmartThingsTypeRegistry() {
        return this.typeRegistry;
    }
}
