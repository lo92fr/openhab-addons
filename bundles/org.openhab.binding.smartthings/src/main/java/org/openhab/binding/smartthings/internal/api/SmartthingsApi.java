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
package org.openhab.binding.smartthings.internal.api;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.sse.SseEventSource;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.smartthings.internal.dto.AppRequest;
import org.openhab.binding.smartthings.internal.dto.AppResponse;
import org.openhab.binding.smartthings.internal.dto.OAuthConfigRequest;
import org.openhab.binding.smartthings.internal.dto.SmartthingsApp;
import org.openhab.binding.smartthings.internal.dto.SmartthingsCapabilitie;
import org.openhab.binding.smartthings.internal.dto.SmartthingsDevice;
import org.openhab.binding.smartthings.internal.dto.SmartthingsLocation;
import org.openhab.binding.smartthings.internal.dto.SmartthingsRoom;
import org.openhab.binding.smartthings.internal.dto.SmartthingsStatus;
import org.openhab.binding.smartthings.internal.type.SmartthingsException;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.osgi.service.jaxrs.client.SseEventSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Class to handle Smartthings Web Api calls.
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public class SmartthingsApi {

    private final Logger logger = LoggerFactory.getLogger(SmartthingsApi.class);

    private final SmartthingsNetworkConnector networkConnector;
    private final OAuthClientService oAuthClientService;
    private final ClientBuilder clientBuilder;
    private final SseEventSourceFactory eventSourceFactory;
    private String token;

    private static final String APP_NAME = "openhabnew0160";
    private Gson gson = new Gson();
    private String baseUrl = "https://api.smartthings.com/v1";
    private String deviceEndPoint = "/devices";
    private String appEndPoint = "/apps";
    private String locationEndPoint = "/locations";
    private String roomsEndPoint = "/rooms";
    private String capabilitiesEndPoint = "/capabilities";

    /**
     * Constructor.
     *
     * @param httpClientFactory The httpClientFactory
     * @param OAuthClientService The oAuthClientService
     * @param token The token to access the API
     */
    public SmartthingsApi(HttpClientFactory httpClientFactory, SmartthingsNetworkConnector networkConnector,
            OAuthClientService oAuthClientService, ClientBuilder clientBuilder,
            SseEventSourceFactory eventSourceFactory, String token) {
        this.token = token;
        this.networkConnector = networkConnector;
        this.oAuthClientService = oAuthClientService;
        this.clientBuilder = clientBuilder;
        this.eventSourceFactory = eventSourceFactory;
    }

    public SmartthingsDevice[] getAllDevices() throws SmartthingsException {
        SmartthingsDevice[] devices = doRequest(SmartthingsDevice[].class, baseUrl + deviceEndPoint);
        return devices;
    }

    public AppResponse setupApp(String redirectUrl) throws SmartthingsException {
        SmartthingsApp[] appList = getAllApps();

        Optional<SmartthingsApp> appOptional = Arrays.stream(appList).filter(x -> APP_NAME.equals(x.appName))
                .findFirst();

        if (appOptional.isPresent()) {
            SmartthingsApp app = appOptional.get(); // Get it from optional
            app = getApp(app.appId);

            AppResponse result = new AppResponse();
            result.app = app;
            result.oauthClientId = null;
            result.oauthClientSecret = null;

            return result;
        } else {
            AppResponse result = createApp(redirectUrl);
            return result;
        }
    }

    public SmartthingsCapabilitie[] getAllCapabilities() throws SmartthingsException {
        try {
            String uri = baseUrl + capabilitiesEndPoint;
            SmartthingsCapabilitie[] listCapabilities = doRequest(SmartthingsCapabilitie[].class, uri);
            return listCapabilities;
        } catch (final Exception e) {
            throw new SmartthingsException("SmartthingsApi : Unable to retrieve capabilities", e);
        }
    }

    public SmartthingsCapabilitie getCapabilitie(String capabilityId, String version) throws SmartthingsException {
        try {
            String uri = baseUrl + capabilitiesEndPoint + "/" + capabilityId + "/" + version;
            SmartthingsCapabilitie capabilitie = doRequest(SmartthingsCapabilitie.class, uri);
            return capabilitie;
        } catch (final Exception e) {
            throw new SmartthingsException("SmartthingsApi : Unable to retrieve capability", e);
        }
    }

    public SmartthingsLocation[] getAllLocations() throws SmartthingsException {
        try {
            String uri = baseUrl + locationEndPoint;
            SmartthingsLocation[] listLocations = doRequest(SmartthingsLocation[].class, uri);
            return listLocations;
        } catch (final Exception e) {
            throw new SmartthingsException("SmartthingsApi : Unable to retrieve locations", e);
        }
    }

    public SmartthingsLocation getLocation(String locationId) throws SmartthingsException {
        try {
            String uri = baseUrl + locationEndPoint + "/" + locationId;

            SmartthingsLocation loc = doRequest(SmartthingsLocation.class, uri);

            return loc;
        } catch (final Exception e) {
            throw new SmartthingsException("SmartthingsApi : Unable to retrieve location", e);
        }
    }

    public SmartthingsRoom[] getRooms(String locationId) throws SmartthingsException {
        try {
            String uri = baseUrl + locationEndPoint + "/" + locationId + roomsEndPoint;
            SmartthingsRoom[] listRooms = doRequest(SmartthingsRoom[].class, uri);
            return listRooms;
        } catch (final Exception e) {
            throw new SmartthingsException("SmartthingsApi : Unable to retrieve rooms", e);
        }
    }

    public SmartthingsRoom getRoom(String locationId, String roomId) throws SmartthingsException {
        try {
            String uri = baseUrl + locationEndPoint + "/" + locationId + roomsEndPoint + "/" + roomId;

            SmartthingsRoom loc = doRequest(SmartthingsRoom.class, uri);

            return loc;
        } catch (final Exception e) {
            throw new SmartthingsException("SmartthingsApi : Unable to retrieve room", e);
        }
    }

    public SmartthingsApp[] getAllApps() throws SmartthingsException {
        try {
            String uri = baseUrl + appEndPoint;

            SmartthingsApp[] listApps = doRequest(SmartthingsApp[].class, uri);

            logger.info("");
            return listApps;
        } catch (final Exception e) {
            throw new SmartthingsException("SmartthingsApi : Unable to retrieve apps", e);
        }
    }

    public SmartthingsApp getApp(String appId) throws SmartthingsException {
        try {
            String uri = baseUrl + appEndPoint + "/" + appId;

            SmartthingsApp app = doRequest(SmartthingsApp.class, uri);

            return app;
        } catch (final Exception e) {
            throw new SmartthingsException("SmartthingsApi : Unable to retrieve app", e);
        }
    }

    public AppResponse createApp(String redirectUrl) throws SmartthingsException {
        try {
            String uri = baseUrl + appEndPoint + "?signatureType=ST_PADLOCK&requireConfirmation=true";
            redirectUrl = redirectUrl + "/cb";

            String appName = APP_NAME;
            AppRequest appRequest = new AppRequest();
            appRequest.appName = appName;
            appRequest.displayName = appName;
            appRequest.description = "Desc " + appName;
            appRequest.appType = "WEBHOOK_SMART_APP";
            appRequest.webhookSmartApp = new AppRequest.webhookSmartApp(redirectUrl);
            appRequest.classifications = new String[1];
            appRequest.classifications[0] = "AUTOMATION";

            String body = gson.toJson(appRequest);
            AppResponse appResponse = doRequest(AppResponse.class, uri, body, false);

            return appResponse;
        } catch (final Exception e) {
            throw new SmartthingsException("SmartthingsApi : Unable to create app", e);
        }
    }

    public void createAppOAuth(String appId) throws SmartthingsException {
        try {
            String uri = baseUrl + appEndPoint + "/" + appId + "/oauth";

            OAuthConfigRequest oAuthConfig = new OAuthConfigRequest();
            oAuthConfig.clientName = "Openhab Integration";
            oAuthConfig.scope = new String[1];
            oAuthConfig.scope[0] = "r:devices:*";

            String body = gson.toJson(oAuthConfig);
            doRequest(JsonObject.class, uri, body, true);

            logger.info("");

            // return appResponse;
        } catch (final Exception e) {
            throw new SmartthingsException("SmartthingsApi : Unable to create oauth settings", e);
        }
    }

    public String getToken() throws SmartthingsException {
        try {
            final AccessTokenResponse accessTokenResponse = oAuthClientService.getAccessTokenResponse();
            final String accessToken = accessTokenResponse == null ? null : accessTokenResponse.getAccessToken();
            if (accessToken.isEmpty()) {
                throw new SmartthingsException(
                        "No Smartthings accesstoken. Did you authorize Smartthings via /connectsmartthings ?");
            }

            return accessToken;

        } catch (OAuthException ex) {
            logger.info("Exception:" + ex.toString());
        } catch (OAuthResponseException ex) {
            logger.info("Exception:" + ex.toString());
        } catch (IOException ex) {
            logger.info("Exception:" + ex.toString());
        }

        return "";
    }

    public String getToken2() throws SmartthingsException {
        String accessToken = token;
        if (accessToken.isEmpty()) {
            throw new SmartthingsException(
                    "No Smartthings accesstoken. Did you authorize Smartthings via /connectsmartthings ?");
        }

        return accessToken;
    }

    public void sendCommand(String deviceId, String jsonMsg) throws SmartthingsException {
        try {
            String uri = baseUrl + deviceEndPoint + "/" + deviceId + "/commands";
            doRequest(JsonObject.class, uri, jsonMsg, false);
        } catch (final Exception e) {
            throw new SmartthingsException("SmartthingsApi : Unable to send command", e);
        }
    }

    public @Nullable SmartthingsStatus getStatus(String deviceId) throws SmartthingsException {
        try {
            String uri = baseUrl + deviceEndPoint + "/" + deviceId + "/status";

            SmartthingsStatus res = doRequest(SmartthingsStatus.class, uri, null, false);
            return res;
        } catch (final Exception e) {
            throw new SmartthingsException("SmartthingsApi : Unable to send status", e);
        }
    }

    public <T> T doRequest(Class<T> resultClass, String uri) throws SmartthingsException {
        return doRequest(resultClass, uri, null, false);
    }

    public <T> T doRequest(Class<T> resultClass, String uri, @Nullable String body, Boolean update)
            throws SmartthingsException {
        try {
            HttpMethod httpMethod = HttpMethod.GET;
            if (body != null) {
                if (update) {
                    httpMethod = HttpMethod.PUT;
                } else {
                    httpMethod = HttpMethod.POST;
                }
            }
            T res = networkConnector.doRequest(resultClass, uri, null, getToken(), body, httpMethod);
            return res;
        } catch (final Exception e) {
            throw new SmartthingsException("SmartthingsApi : Unable to do request", e);
        }
    }

    public void registerSubscriptions(String tokenInstallUpdate, String locationId) {
        try {

            String installedAppId = "createAppOAuth";
            // String installedAppId = "ecc7b4e4-b895-4d03-b13d-003ce45aaee1";
            // String subscriptionUri = "https://api.smartthings.com/v1/installedapps/" + installedAppId
            // + "/subscriptions";

            String subscriptionUri = "https://api.smartthings.com/subscriptions";

            // Remove old subscriptions before recreating them
            // networkConnector.doRequest(JsonObject.class, subscriptionUri, null, tokenInstallUpdate, "",
            // HttpMethod.DELETE);

            // networkConnector.doRequest(JsonObject.class, subscriptionUri, null, tokenInstallUpdate, "",
            // HttpMethod.GET);

            // SMEvent evt = new SMEvent();
            // evt.sourceType = "DEVICE";
            // evt.device = new device("dev.deviceId", "main", true, null);

            // String body = gson.toJson(evt);
            String body = "";

            body += "{";
            body += "   \"name\": \"My Home Assistant sub\",";
            body += "   \"version\": 20250122,";
            body += "   \"clientDeviceId\": \"iapp_" + installedAppId + "\",";
            body += "   \"subscriptionFilters\":";
            body += "      [";
            body += "         {";
            body += "             \"type\": \"LOCATIONIDS\",";
            body += "             \"value\": [\"cb73e411-15b4-40e8-b6cd-f9a34f6ced4b\"],";
            body += "             \"eventType\": [";
            body += "                       \"DEVICE_EVENT\",";
            body += "                       \"DEVICE_LIFECYCLE_EVENT\",";
            body += "                       \"DEVICE_HEALTH_EVENT\"";
            body += "             ]";
            body += "         }";
            body += "      ]";
            body += "}";

            logger.debug("body:" + body);
            JsonObject result = networkConnector.doRequest(JsonObject.class, subscriptionUri, null, getToken(), body,
                    HttpMethod.POST);
            String uri = result.get("registrationUrl").getAsString();

            // uri = "https://stream.wikimedia.org/v2/stream/recentchange";

            token = getToken();
            // Client client = clientBuilder.build();

            Client client = clientBuilder.build().register(new ClientRequestFilter() {
                @Override
                public void filter(@Nullable ClientRequestContext requestContext) throws IOException {
                    requestContext.getHeaders().add("Authorization", "Bearer " + token);
                }
            });

            String targetUrl = UriBuilder.fromUri(uri).build().toString();

            WebTarget target = client.target(targetUrl);

            try (SseEventSource source = eventSourceFactory.newBuilder(target).build()) {
                source.register(event -> System.out.println("Received: " + event.readData()),
                        ex -> ex.printStackTrace(), () -> System.out.println("Stream closed."));
                source.open();

                Thread.sleep(60000);
            }

            logger.debug("result");
        } catch (Exception ex) {
            logger.debug("ex:" + ex.toString());
        }

    }
}
