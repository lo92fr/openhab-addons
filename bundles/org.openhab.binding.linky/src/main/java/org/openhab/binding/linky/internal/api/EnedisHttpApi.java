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
package org.openhab.binding.linky.internal.api;

import java.io.IOException;
import java.net.HttpCookie;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.core.MediaType;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.FormContentProvider;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.Fields;
import org.openhab.binding.linky.internal.config.LinkyBridgeWebConfiguration;
import org.openhab.binding.linky.internal.config.LinkyThingRemoteConfiguration;
import org.openhab.binding.linky.internal.constants.LinkyBindingConstants;
import org.openhab.binding.linky.internal.dto.Alimentation;
import org.openhab.binding.linky.internal.dto.AuthData;
import org.openhab.binding.linky.internal.dto.AuthResult;
import org.openhab.binding.linky.internal.dto.ConsumptionReport;
import org.openhab.binding.linky.internal.dto.Contact;
import org.openhab.binding.linky.internal.dto.Contract;
import org.openhab.binding.linky.internal.dto.ContractSynthese;
import org.openhab.binding.linky.internal.dto.GeneralData;
import org.openhab.binding.linky.internal.dto.Identity;
import org.openhab.binding.linky.internal.dto.MeterReading;
import org.openhab.binding.linky.internal.dto.PrmDetail;
import org.openhab.binding.linky.internal.dto.PrmInfo;
import org.openhab.binding.linky.internal.dto.ResponseContact;
import org.openhab.binding.linky.internal.dto.ResponseContract;
import org.openhab.binding.linky.internal.dto.ResponseIdentity;
import org.openhab.binding.linky.internal.dto.ResponseMeter;
import org.openhab.binding.linky.internal.dto.ResponseTempo;
import org.openhab.binding.linky.internal.dto.ServiceSoucrits;
import org.openhab.binding.linky.internal.dto.UsagePoint;
import org.openhab.binding.linky.internal.dto.UserInfo;
import org.openhab.binding.linky.internal.handler.BridgeRemoteApiHandler;
import org.openhab.binding.linky.internal.handler.BridgeRemoteBaseHandler;
import org.openhab.binding.linky.internal.handler.ThingBaseRemoteHandler;
import org.openhab.binding.linky.internal.handler.ThingLinkyRemoteHandler;
import org.openhab.binding.linky.internal.types.LinkyException;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * {@link EnedisHttpApi} wraps the Enedis Webservice.
 *
 * @author Gaël L'hopital - Initial contribution
 * @author Laurent Arnal - Rewrite addon to use official dataconect API
 */
@NonNullByDefault
public class EnedisHttpApi {
    private final Logger logger = LoggerFactory.getLogger(EnedisHttpApi.class);
    private final Gson gson;
    private final HttpClient httpClient;
    private final BridgeRemoteBaseHandler linkyBridgeHandler;

    private ApiConfig apiConfig;

    private OAuthFactory oAuthFactory;
    private String idPersonne = "";
    private boolean connected = false;
    private final URI cookieUri;
    private @Nullable OAuthClientService oAuthService;

    public EnedisHttpApi(BridgeRemoteBaseHandler linkyBridgeHandler, ApiConfig apiConfig, Gson gson,
            OAuthFactory oAuthFactory, HttpClient httpClient) {
        this.gson = gson;
        this.httpClient = httpClient;
        this.linkyBridgeHandler = linkyBridgeHandler;
        this.oAuthFactory = oAuthFactory;

        this.apiConfig = apiConfig;
        cookieUri = URI.create(apiConfig.urlMonCompteParticulier);
    }

    public synchronized void connectionInit() throws LinkyException {
        if (apiConfig.useOAuth) {
            this.oAuthService = oAuthFactory.createOAuthClientService(LinkyBindingConstants.BINDING_ID,
                    apiConfig.tokenUrl, apiConfig.authorizeUrl, apiConfig.clientId, apiConfig.clientSecret,
                    LinkyBindingConstants.LINKY_SCOPES, true);
            this.connected = true;
            return;
        }

        logger.debug("Starting login process for user: {}", apiConfig.userName);

        try {
            ContentResponse result = null;
            String uri = "";
            String gotoUri = "";

            // has we reconnect, remove all previous cookie to start from fresh session
            removeAllCookie();

            addCookie(LinkyBridgeWebConfiguration.INTERNAL_AUTH_ID, apiConfig.internalAuthId);

            // ======================================================
            logger.debug("Step 1a: getting authentification");
            // ======================================================
            uri = apiConfig.urlEnedisAuthenticate;
            result = httpClient.GET(uri);

            if (result.getStatus() != HttpStatus.MOVED_TEMPORARILY_302) {
                throw new LinkyException("Connection failed step 1a - auth1: %d %s", result.getStatus(),
                        result.getContentAsString());
            }

            // ======================================================
            logger.debug("Step 1b: ...");
            // ======================================================
            uri = apiConfig.baseUrl + result.getHeaders().get("Location");
            result = httpClient.GET(uri);

            if (result.getStatus() != HttpStatus.MOVED_TEMPORARILY_302) {
                throw new LinkyException("Connection failed step 1b - auth1: %d %s", result.getStatus(),
                        result.getContentAsString());
            }

            // ======================================================
            logger.debug("Step 1c: ...");
            // ======================================================
            uri = result.getHeaders().get("Location");

            result = httpClient.GET(uri);

            if (result.getStatus() != HttpStatus.MOVED_TEMPORARILY_302) {
                throw new LinkyException("Connection failed step 1c - auth1: %d %s", result.getStatus(),
                        result.getContentAsString());
            }

            // ======================================================
            logger.debug("Step 1d: ...");
            // ======================================================
            uri = result.getHeaders().get("Location");
            int idx = uri.indexOf("goto=");
            gotoUri = uri.substring(idx + 5);

            result = httpClient.GET(uri);

            if (result.getStatus() != HttpStatus.MOVED_TEMPORARILY_302) {
                throw new LinkyException("Connection failed step 1d - auth1: %d %s", result.getStatus(),
                        result.getContentAsString());
            }

            // ======================================================
            logger.debug("Step 1e: ...");
            // ======================================================
            uri = apiConfig.urlMonCompte + result.getHeaders().get("Location");
            result = httpClient.GET(uri);

            if (result.getStatus() != HttpStatus.OK_200) {
                throw new LinkyException("Connection failed step 1e - auth1: %d %s", result.getStatus(),
                        result.getContentAsString());
            }

            // ======================================================
            logger.debug("Step 2: auth1 - retrieve the template, thanks to cookie internalAuthId user is already set");
            // ======================================================
            uri = apiConfig.urlMonCompte + "/auth/json/authenticate?realm=/enedis&goto=" + gotoUri;

            result = httpClient.POST(uri).header("X-NoSession", "true").header("X-Password", "anonymous")
                    .header("X-Requested-With", "XMLHttpRequest").header("X-Username", "anonymous").send();
            if (result.getStatus() != HttpStatus.OK_200) {
                throw new LinkyException("Connection failed step 3 - auth1: %s", result.getContentAsString());
            }

            AuthData authData = gson.fromJson(result.getContentAsString(), AuthData.class);
            if (authData != null) {
                if (authData.callbacks.size() < 2 || authData.callbacks.get(0).input.isEmpty()
                        || authData.callbacks.get(1).input.isEmpty() || !apiConfig.userName.equals(
                                Objects.requireNonNull(authData.callbacks.get(0).input.get(0)).valueAsString())) {
                    logger.debug("auth1 - invalid template for auth data: {}", result.getContentAsString());
                    throw new LinkyException("Authentication error, the authentication_cookie is probably wrong");
                }

                authData.callbacks.get(1).input.get(0).value = apiConfig.password;
            }

            // ======================================================
            logger.debug("Step 3: auth2 - send the auth data");
            // ======================================================
            result = httpClient.POST(uri).header(HttpHeader.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                    .header("X-NoSession", "true").header("X-Password", "anonymous")
                    .header("X-Requested-With", "XMLHttpRequest").header("X-Username", "anonymous")
                    .content(new StringContentProvider(gson.toJson(authData))).send();
            if (result.getStatus() != HttpStatus.OK_200) {
                throw new LinkyException("Connection failed step 3 - auth2 : %s", result.getContentAsString());
            }

            AuthResult authResult = gson.fromJson(result.getContentAsString(), AuthResult.class);
            if (authResult != null && authResult.successUrl == null) {
                authData = gson.fromJson(result.getContentAsString(), AuthData.class);
                if (authData == null) {
                    throw new LinkyException("Errors on step3 : authData=null");
                }
                boolean invalidCredentials = authData.callbacks.stream()
                        .filter(callback -> "TextOutputCallback".equals(callback.type))
                        .flatMap(callback -> callback.output.stream()).anyMatch(output -> "message".equals(output.name)
                                && "Identifiant et/ou mot de passe erroné(s)".equals(output.value));
                if (invalidCredentials) {
                    throw new LinkyException("Identifiant et/ou mot de passe erroné(s)");
                }
            }

            logger.debug("Add the tokenId cookie");
            if (authResult == null) {
                throw new LinkyException("Errors on step3 : authResult=null");
            }

            addCookie("enedisExt", authResult.tokenId);
            // ======================================================
            logger.debug("Step 4a: Confirm login");
            // ======================================================
            uri = authResult.successUrl;
            result = httpClient.GET(uri);
            if (result.getStatus() != HttpStatus.MOVED_TEMPORARILY_302) {
                throw new LinkyException("Connection failed step 4a - auth2: %d %s", result.getStatus(),
                        result.getContentAsString());
            }

            // ======================================================
            logger.debug("Step 4b:Confirm login");
            // ======================================================
            uri = result.getHeaders().get("Location");
            result = httpClient.GET(uri);
            if (result.getStatus() != HttpStatus.MOVED_TEMPORARILY_302) {
                throw new LinkyException("Connection failed step 4b - auth2: %d %s", result.getStatus(),
                        result.getContentAsString());
            }

            // ======================================================
            logger.debug("Step 4c: Confirm login");
            // ======================================================
            uri = apiConfig.baseUrl
                    + "/authenticate?target=https://mon-compte-client.enedis.fr%2Fhub%3FallEspace%3Dfalse";
            // "result.getHeaders().get("Location");

            result = httpClient.GET(uri);
            if (result.getStatus() != HttpStatus.TEMPORARY_REDIRECT_307) {
                throw new LinkyException("Connection failed step 4c - auth2: %d %s", result.getStatus(),
                        result.getContentAsString());
            }

            // ===========================================================
            logger.debug("Step 5: retrieve user information andd cookie");
            // ===========================================================
            result = httpClient.GET(apiConfig.userInfoContractUrl);

            @SuppressWarnings("unchecked")
            HashMap<String, String> hashRes = gson.fromJson(result.getContentAsString(), HashMap.class);

            String cookieKey;

            if (hashRes != null && hashRes.containsKey("cnAlex")) {
                cookieKey = "personne_for_" + hashRes.get("cnAlex");
                idPersonne = Objects.requireNonNull(hashRes.get("idPersonne"));
            } else {
                throw new LinkyException("Connection failed step 5, missing cookieKey");
            }

            List<HttpCookie> lCookie = httpClient.getCookieStore().getCookies();
            Optional<HttpCookie> cookie = lCookie.stream().filter(it -> it.getName().contains(cookieKey)).findFirst();

            String cookieVal = cookie.map(HttpCookie::getValue)
                    .orElseThrow(() -> new LinkyException("Connection failed step 7, missing cookieVal"));

            addCookie(cookieKey, cookieVal);

            connected = true;
        } catch (InterruptedException | TimeoutException | ExecutionException | JsonSyntaxException e) {
            throw new LinkyException(e, "Error opening connection with Enedis webservice");
        }
    }

    public FormContentProvider getFormContent(String fieldName, String fieldValue) {
        Fields fields = new Fields();
        fields.put(fieldName, fieldValue);
        return new FormContentProvider(fields);
    }

    public void addCookie(String key, String value) {
        HttpCookie cookie = new HttpCookie(key, value);
        cookie.setDomain(apiConfig.enedisDomain);
        cookie.setPath("/");
        httpClient.getCookieStore().add(cookieUri, cookie);
    }

    public void removeAllCookie() {
        httpClient.getCookieStore().removeAll();
    }

    public String getLocation(ContentResponse response) {
        return response.getHeaders().get(HttpHeader.LOCATION);
    }

    public String getContent(String url, String token, String body) throws LinkyException {
        return getContent(logger, url, httpClient, token, body);
    }

    public String getContent(String url) throws LinkyException {
        return getContent(logger, url, httpClient, "", "");
    }

    private String getContent(Logger logger, String url, HttpClient httpClient, String token, String body)
            throws LinkyException {
        try {
            Request request = httpClient.newRequest(url);

            request = request.agent("Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:47.0) Gecko/20100101 Firefox/47.0");

            if (!token.isEmpty()) {
                request = request.header("Authorization", "" + token);
                request = request.header("Accept", "application/json");
            }

            if (body != null && !body.isEmpty()) {
                request = request.content(new StringContentProvider(body));
                request = request.header("Content-Type", "application/json");
                request = request.method(HttpMethod.POST);
            } else {
                request = request.method(HttpMethod.GET);
            }

            ContentResponse result = request.send();
            if (result.getStatus() == HttpStatus.TEMPORARY_REDIRECT_307
                    || result.getStatus() == HttpStatus.MOVED_TEMPORARILY_302) {
                String loc = result.getHeaders().get("Location");
                String newUrl = "";

                if (loc.startsWith("http://") || loc.startsWith("https://")) {
                    newUrl = loc;
                } else {
                    newUrl = apiConfig.baseUrl + loc.substring(1);
                }

                request = httpClient.newRequest(newUrl);
                request = request.method(HttpMethod.GET);
                result = request.send();

                if (result.getStatus() == HttpStatus.TEMPORARY_REDIRECT_307
                        || result.getStatus() == HttpStatus.MOVED_TEMPORARILY_302) {
                    loc = result.getHeaders().get("Location");
                    String[] urlParts = loc.split("/");
                    if (urlParts.length < 4) {
                        throw new LinkyException("malformed url : %s", loc);
                    }
                    return urlParts[3];
                }
            }
            if (result.getStatus() != 200) {
                throw new LinkyException("Error requesting '%s': %s", url, result.getContentAsString());
            }

            String content = result.getContentAsString();
            logger.trace("getContent returned {}", content);
            return content;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new LinkyException(e, "Error getting url: '%s'", url);
        }
    }

    private <T> T getData(ThingBaseRemoteHandler handler, String url, Class<T> clazz) throws LinkyException {
        return getData(handler, url, clazz, "");
    }

    private <T> T getData(ThingBaseRemoteHandler handler, String url, Class<T> clazz, String body)
            throws LinkyException {
        if (!connected) {
            connectionInit();
        }

        int numberRetry = 0;
        LinkyException lastException = null;
        logger.debug("getData begin {}: {}", clazz.getName(), url);

        String token = "";

        if (apiConfig.useOAuth) {
            token = getToken(handler);
        }

        while (numberRetry < 3) {
            try {
                String data = getContent(url, token, body);

                if (!data.isEmpty()) {
                    try {
                        T result = Objects.requireNonNull(gson.fromJson(data, clazz));
                        logger.trace("getData success {}: {}", clazz.getName(), url);
                        return result;
                    } catch (JsonSyntaxException e) {
                        logger.debug("Invalid JSON response not matching {}: {}", clazz.getName(), data);
                        throw new LinkyException(e, "Requesting '%s' returned an invalid JSON response", url);
                    }
                }
            } catch (LinkyException ex) {
                lastException = ex;

                logger.debug("getData error {}: {} , retry{}", clazz.getName(), url, numberRetry);

                // try to reinit connection, fail after 3 attemps
                connectionInit();
            }
            numberRetry++;
        }

        logger.debug("getData error {}: {} , maxRetry", clazz.getName(), url);

        throw Objects.requireNonNull(lastException);
    }

    public PrmInfo getPrmInfo(ThingLinkyRemoteHandler handler, String internId, String prmId) throws LinkyException {
        String prmInfoUrl = apiConfig.contractUrl.formatted(internId);
        PrmInfo[] prms = getData(handler, prmInfoUrl, PrmInfo[].class);
        if (prms.length < 1) {
            throw new LinkyException("Invalid prms data received");
        }

        if (prmId.isBlank()) {
            return prms[0];
        }

        Optional<PrmInfo> result = Arrays.stream(prms).filter(x -> x.idPrm.equals(prmId)).findFirst();
        if (result.isPresent()) {
            return result.get();
        }

        throw new LinkyException(("PRM with id : %s does not exist").formatted(prmId));
    }

    public PrmDetail getPrmDetails(ThingLinkyRemoteHandler handler, String internId, String prmId)
            throws LinkyException {
        String url = apiConfig.contractUrl.formatted(internId) + "/" + prmId
                + "?embed=SITALI&embed=SITCOM&embed=SITCON&embed=SYNCON";
        return getData(handler, url, PrmDetail.class);
    }

    public UserInfo getUserInfo(ThingLinkyRemoteHandler handler) throws LinkyException {
        return getData(handler, apiConfig.contactUrl, UserInfo.class);
    }

    public String formatUrl(String apiUrl, String prmId) {
        return apiUrl.formatted(prmId);
    }

    public Contract getContract(ThingLinkyRemoteHandler handler, String prmId) throws LinkyException {

        String body = "{ \"comptage\": false,  \"autorisationId\" : 11 }";
        ServiceSoucrits serviceSouscrits = getData(handler, apiConfig.subscribedServiceUrl, ServiceSoucrits.class,
                body);

        ResponseContract[] contractResponse = getData(handler, apiConfig.contractUrl.formatted(prmId),
                ResponseContract[].class);

        String addressUrl = apiConfig.adressUrl.formatted(prmId);
        GeneralData generalData = getData(handler, addressUrl, GeneralData.class);

        ContractSynthese contractSynthese = getData(handler, apiConfig.contractSyntheseUrl.formatted(prmId),
                ContractSynthese.class);

        Alimentation alimentation = getData(handler, apiConfig.alimentationUrl.formatted(prmId), Alimentation.class);

        return new Contract();
    }

    public UsagePoint getUsagePoint(ThingLinkyRemoteHandler handler, String prmId) throws LinkyException {
        String addressUrl = apiConfig.adressUrl.formatted(prmId);
        ResponseContract contractResponse = getData(handler, addressUrl, ResponseContract.class);
        // return contractResponse.customer.usagePoint[0].usagePoint;
        return new UsagePoint();
    }

    public Identity getIdentity(ThingLinkyRemoteHandler handler, String prmId) throws LinkyException {
        ResponseIdentity customerIdReponse = getData(handler, apiConfig.identityUrl.formatted(prmId),
                ResponseIdentity.class);
        String name = customerIdReponse.identity.naturalPerson.lastname;
        String[] nameParts = name.split(" ");
        if (nameParts.length > 1) {
            customerIdReponse.identity.naturalPerson.firstname = name.split(" ")[0];
            customerIdReponse.identity.naturalPerson.lastname = name.split(" ")[1];
        }
        return customerIdReponse.identity.naturalPerson;
    }

    public Contact getContact(ThingLinkyRemoteHandler handler, String prmId) throws LinkyException {
        ResponseContact contactResponse = getData(handler, apiConfig.contactUrl.formatted(prmId),
                ResponseContact.class);
        return contactResponse.contact;
    }

    private MeterReading getMeasures(ThingLinkyRemoteHandler handler, String apiUrl, String mps, String prmId,
            String segment, LocalDate from, LocalDate to, boolean useIndex) throws LinkyException {
        String dtStart = from.format(apiConfig.apiDateFormat);
        String dtEnd = to.format(apiConfig.apiDateFormat);

        if (linkyBridgeHandler instanceof BridgeRemoteApiHandler) {
            String url = String.format(apiUrl, prmId, dtStart, dtEnd);
            ResponseMeter meterResponse = getData(handler, url, ResponseMeter.class);
            return meterResponse.meterReading;
        } else {
            String url = String.format(apiUrl, mps, prmId, segment, dtStart, dtEnd);
            ConsumptionReport consomptionReport = getData(handler, url, ConsumptionReport.class);
            return MeterReading.convertFromComsumptionReport(consomptionReport, useIndex);
        }
    }

    public MeterReading getEnergyData(ThingLinkyRemoteHandler handler, String mps, String prmId, String segment,
            LocalDate from, LocalDate to) throws LinkyException {
        return getMeasures(handler, apiConfig.dailyConsumptionUrl, mps, prmId, segment, from, to, false);
    }

    public MeterReading getEnergyIndex(ThingLinkyRemoteHandler handler, String mps, String prmId, String segment,
            LocalDate from, LocalDate to) throws LinkyException {
        return getMeasures(handler, apiConfig.dailyIndexUrl, mps, prmId, segment, from, to, true);
    }

    public MeterReading getLoadCurveData(ThingLinkyRemoteHandler handler, String mps, String prmId, String segment,
            LocalDate from, LocalDate to) throws LinkyException {
        return getMeasures(handler, apiConfig.loadCurveUrl, mps, prmId, segment, from, to, false);
    }

    public MeterReading getPowerData(ThingLinkyRemoteHandler handler, String mps, String prmId, String segment,
            LocalDate from, LocalDate to) throws LinkyException {
        return getMeasures(handler, apiConfig.maxPowerUrl, mps, prmId, segment, from, to, false);
    }

    public ResponseTempo getTempoData(ThingBaseRemoteHandler handler, LocalDate from, LocalDate to)
            throws LinkyException {
        String dtStart = from.format(apiConfig.apiDateFormatYearsFirst);
        String dtEnd = to.format(apiConfig.apiDateFormatYearsFirst);

        String url = String.format(apiConfig.tempoUrl, dtStart, dtEnd);

        if (url.isEmpty()) {
            return new ResponseTempo();
        }

        ResponseTempo responseTempo = getData(handler, url, ResponseTempo.class);
        return responseTempo;
    }

    public boolean isConnected() {
        return connected;
    }

    public String getIdPersonne() {
        return idPersonne;
    }

    // public String authorize1(String redirectUri, String reqState, String reqCode) throws LinkyException {
    // String url = String.format(tokenUrl, clientId, reqCode);
    // String token = getContent(url);
    //
    // logger.debug("token: {}", token);
    //
    // Collection<Thing> col = this.getThing().getThings();
    //
    // for (Thing thing : col) {
    // if (LinkyBindingConstants.THING_TYPE_LINKY.equals(thing.getThingTypeUID())) {
    // Configuration config = thing.getConfiguration();
    // String prmId = (String) config.get("prmId");
    //
    // if (!prmId.equals(reqCode)) {
    // continue;
    // }
    //
    // config.put("token", token);
    // ThingLinkyRemoteHandler handler = (ThingLinkyRemoteHandler) thing.getHandler();
    // if (handler != null) {
    // handler.saveConfiguration(config);
    // }
    // }
    // }
    // return token;
    // }

    public String authorize(String redirectUri, String reqState, String reqCode) throws LinkyException {
        // Will work only in case of direct oAuth2 authentication to enedis
        // this is not the case in v1 as we go through MyElectricalData

        try {
            logger.debug("Make call to Enedis to get access token.");
            OAuthClientService lcOAuthService = this.oAuthService;
            if (lcOAuthService == null) {
                return "";
            }

            final AccessTokenResponse credentials = lcOAuthService
                    .getAccessTokenByClientCredentials(LinkyBindingConstants.LINKY_SCOPES);

            String accessToken = credentials.getAccessToken();
            if (accessToken == null) {
                throw new LinkyException("Unable to authenticate, no access token available");
            }

            // avoid logging the full access token for security reasons
            logger.debug("Access token: {}...", accessToken.substring(0, 3));
            return accessToken;
        } catch (RuntimeException | OAuthException | IOException e) {
            throw new LinkyException("Error during oAuth authorize :" + e.getMessage(), e);
        } catch (final OAuthResponseException e) {
            throw new LinkyException("Error during oAuth authorize :" + e.getMessage(), e);
        }
    }

    public boolean isAuthorized() {
        final AccessTokenResponse accessTokenResponse = getAccessTokenResponse();

        return accessTokenResponse != null && accessTokenResponse.getAccessToken() != null
                && accessTokenResponse.getRefreshToken() != null;
    }

    protected @Nullable AccessTokenResponse getAccessTokenByClientCredentials() {
        try {
            OAuthClientService lcOAuthService = this.oAuthService;
            if (lcOAuthService == null) {
                return null;
            }

            return lcOAuthService.getAccessTokenByClientCredentials(LinkyBindingConstants.LINKY_SCOPES);
        } catch (OAuthException | IOException | OAuthResponseException | RuntimeException e) {
            logger.debug("Exception checking authorization: ", e);
            return null;
        }
    }

    public String getTokenFromConfig(ThingBaseRemoteHandler handler) throws LinkyException {
        if (handler.getLinkyConfig() instanceof LinkyThingRemoteConfiguration config) {
            return config.token;
        }
        return "";
    }

    public String getToken(ThingBaseRemoteHandler handler) throws LinkyException {
        AccessTokenResponse accesToken = getAccessTokenResponse();

        // Store token is about to expire, ask for a new one.
        if (accesToken != null && accesToken.isExpired(Instant.now(), 1200)) {
            accesToken = null;
        }

        if (accesToken == null) {
            accesToken = getAccessTokenByClientCredentials();
        }

        if (accesToken == null) {
            throw new LinkyException("no token");
        }

        return "Bearer " + accesToken.getAccessToken();
    }

    protected @Nullable AccessTokenResponse getAccessTokenResponse() {
        try {
            OAuthClientService lcOAuthService = this.oAuthService;
            if (lcOAuthService == null) {
                return null;
            }

            return lcOAuthService.getAccessTokenResponse();
        } catch (OAuthException | IOException | OAuthResponseException | RuntimeException e) {
            logger.debug("Exception checking authorization: ", e);
            return null;
        }
    }

    public String formatAuthorizationUrl(String redirectUri) {
        try {
            OAuthClientService lcOAuthService = this.oAuthService;
            if (lcOAuthService == null) {
                return "";
            }

            String uri = lcOAuthService.getAuthorizationUrl(redirectUri, LinkyBindingConstants.LINKY_SCOPES,
                    LinkyBindingConstants.BINDING_ID);
            return uri;
        } catch (final OAuthException e) {
            logger.debug("Error constructing AuthorizationUrl: ", e);
            return "";
        }
    }

    public ApiConfig getApiConfig() {
        return apiConfig;
    }

}
