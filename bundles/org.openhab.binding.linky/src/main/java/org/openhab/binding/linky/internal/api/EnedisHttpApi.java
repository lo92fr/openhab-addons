/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import java.net.HttpCookie;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.core.MediaType;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.FormContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.Fields;
import org.openhab.binding.linky.internal.LinkyException;
import org.openhab.binding.linky.internal.dto.AddressInfo;
import org.openhab.binding.linky.internal.dto.ContactInfo;
import org.openhab.binding.linky.internal.dto.Contracts;
import org.openhab.binding.linky.internal.dto.Customer;
import org.openhab.binding.linky.internal.dto.CustomerIdResponse;
import org.openhab.binding.linky.internal.dto.CustomerReponse;
import org.openhab.binding.linky.internal.dto.IdentityInfo;
import org.openhab.binding.linky.internal.dto.MeterReading;
import org.openhab.binding.linky.internal.dto.MeterResponse;
import org.openhab.binding.linky.internal.dto.PrmInfo;
import org.openhab.binding.linky.internal.dto.TempoResponse;
import org.openhab.binding.linky.internal.dto.UsagePoint;
import org.openhab.binding.linky.internal.dto.UsagePointDetails;
import org.openhab.binding.linky.internal.dto.WebPrmInfo;
import org.openhab.binding.linky.internal.dto.WebUserInfo;
import org.openhab.binding.linky.internal.handler.ApiBridgeHandler;
import org.openhab.binding.linky.internal.handler.LinkyHandler;
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

    private static final DateTimeFormatter API_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final Logger logger = LoggerFactory.getLogger(EnedisHttpApi.class);
    private final Gson gson;
    private final HttpClient httpClient;
    private ApiBridgeHandler apiBridgeHandler;

    public static final String ENEDIS_DOMAIN = ".enedis.fr";
    public static final String URL_MON_COMPTE = "https://mon-compte" + EnedisHttpApi.ENEDIS_DOMAIN;
    public static final String URL_COMPTE_PART = URL_MON_COMPTE.replace("compte", "compte-particulier");
    public static final URI COOKIE_URI = URI.create(URL_COMPTE_PART);

    private static final String URL_APPS_LINCS = "https://alex.microapplications" + EnedisHttpApi.ENEDIS_DOMAIN;
    private static final String USER_INFO_URL = URL_APPS_LINCS + "/userinfos";
    private static final String PRM_INFO_BASE_URL = URL_APPS_LINCS + "/mes-mesures/api/private/v1/personnes/";
    private static final String PRM_INFO_URL = PRM_INFO_BASE_URL + "null/prms";
    private static final String MEASURE_URL = PRM_INFO_BASE_URL
            + "%s/prms/%s/donnees-%s?dateDebut=%s&dateFin=%s&mesuretypecode=CONS";

    public EnedisHttpApi(ApiBridgeHandler apiBridgeHandler, Gson gson, HttpClient httpClient) {
        this.gson = gson;
        this.httpClient = httpClient;
        this.apiBridgeHandler = apiBridgeHandler;
    }

    public FormContentProvider getFormContent(String fieldName, String fieldValue) {
        Fields fields = new Fields();
        fields.put(fieldName, fieldValue);
        return new FormContentProvider(fields);
    }

    public void addCookie(String key, String value) {
        HttpCookie cookie = new HttpCookie(key, value);
        cookie.setDomain(ENEDIS_DOMAIN);
        cookie.setPath("/");
        httpClient.getCookieStore().add(COOKIE_URI, cookie);
    }

    public String getLocation(ContentResponse response) {
        return response.getHeaders().get(HttpHeader.LOCATION);
    }

    public String getData(LinkyHandler handler, String url) throws LinkyException {
        return getData(apiBridgeHandler, url, httpClient, apiBridgeHandler.getToken(handler));
    }

    public String getData(String url) throws LinkyException {
        return getData(apiBridgeHandler, url, httpClient, "");
    }

    private static String getData(ApiBridgeHandler apiBridgeHandler, String url, HttpClient httpClient, String token)
            throws LinkyException {
        try {
            Request request = httpClient.newRequest(url);
            request = request.method(HttpMethod.GET);
            if (!("".equals(token))) {
                request = request.header("Authorization", "" + token);
                request = request.header("Accept", "application/json");
            }

            ContentResponse result = request.send();
            if (result.getStatus() == 307) {
                String loc = result.getHeaders().get("Location");
                String newUrl = apiBridgeHandler.getBaseUrl() + loc.substring(1);
                request = httpClient.newRequest(newUrl);
                request = request.method(HttpMethod.GET);
                result = request.send();

                if (result.getStatus() == 307) {
                    loc = result.getHeaders().get("Location");
                    String res = loc.split("/")[3];
                    return res;
                }
            }
            if (result.getStatus() != 200) {
                throw new LinkyException("Error requesting '%s' : %s", url, result.getContentAsString());
            }
            String content = result.getContentAsString();
            logger.trace("getContent returned {}", content);
            return content;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new LinkyException(e, "Error getting url: '%s'", url);
        }
    }

    /*
     * private String getData(String url) throws LinkyException {
     * try {
     * ContentResponse result = httpClient.GET(url);
     * if (result.getStatus() != 200) {
     * throw new LinkyException("Error requesting '%s' : %s", url, result.getContentAsString());
     * }
     * return result.getContentAsString();
     * } catch (InterruptedException | ExecutionException | TimeoutException e) {
     * throw new LinkyException(e, "Error getting url : '%s'", url);
     * }
     * }
     */

    public PrmInfo getPrmInfo(LinkyHandler handler, String prmId) throws LinkyException {
        if (!apiBridgeHandler.isConnected()) {
            apiBridgeHandler.initialize();
        }

        PrmInfo result = new PrmInfo();
        if (false) {
            Customer customer = getCustomer(handler, prmId);
            UsagePoint usagePoint = customer.usagePoints[0];

            result.contractInfo = usagePoint.contracts;
            result.usagePointInfo = usagePoint.usagePoint;
            result.identityInfo = getIdentity(handler, prmId);
            result.addressInfo = getAddress(handler, prmId);
            result.contactInfo = getContact(handler, prmId);

            result.prmId = result.usagePointInfo.usagePointId;
            result.customerId = customer.customerId;

            return result;
        } else {
            WebPrmInfo[] webPrmsInfo;
            WebUserInfo webUserInfo;

            String data = getData(PRM_INFO_URL);
            if (data.isEmpty()) {
                throw new LinkyException("Requesting '%s' returned an empty response", PRM_INFO_URL);
            }
            try {
                webPrmsInfo = gson.fromJson(data, WebPrmInfo[].class);
                if (webPrmsInfo == null || webPrmsInfo.length < 1) {
                    throw new LinkyException("Invalid prms data received");
                }
            } catch (JsonSyntaxException e) {
                logger.debug("invalid JSON response not matching PrmInfo[].class: {}", data);
                throw new LinkyException(e, "Requesting '%s' returned an invalid JSON response", PRM_INFO_URL);
            }

            data = getData(USER_INFO_URL);
            if (data.isEmpty()) {
                throw new LinkyException("Requesting '%s' returned an empty response", USER_INFO_URL);
            }
            try {
                webUserInfo = gson.fromJson(data, WebUserInfo.class);
            } catch (JsonSyntaxException e) {
                logger.debug("invalid JSON response not matching UserInfo.class: {}", data);
                throw new LinkyException(e, "Requesting '%s' returned an invalid JSON response", USER_INFO_URL);
            }

            WebPrmInfo webPrmInfo = Arrays.stream(webPrmsInfo).filter(x -> x.prmId.equals(prmId)).findAny()
                    .orElseThrow();

            result.addressInfo = new AddressInfo();
            result.addressInfo.street = webPrmInfo.adresse.adresseLigneQuatre;
            result.addressInfo.city = webPrmInfo.adresse.adresseLigneSix;
            result.addressInfo.postalCode = webPrmInfo.adresse.adresseLigneSix;
            result.addressInfo.country = webPrmInfo.adresse.adresseLigneSept;

            result.contactInfo = new ContactInfo();
            result.identityInfo = new IdentityInfo();
            result.contractInfo = new Contracts();
            result.usagePointInfo = new UsagePointDetails();

            result.contactInfo.email = webUserInfo.userProperties.mail;
            result.contactInfo.phone = "";

            result.identityInfo.firstname = webUserInfo.userProperties.firstName;
            result.identityInfo.lastname = webUserInfo.userProperties.name;
            result.identityInfo.title = "";
            // webUserInfo.userProperties.internId;
            // webUserInfo.userProperties.personalInfo;

            result.contractInfo.contractStatus = "";
            result.contractInfo.contractType = "";
            result.contractInfo.distributionTariff = "";
            result.contractInfo.lastActivationDate = "";
            result.contractInfo.lastDistributionTariffChangeDate = "";
            result.contractInfo.offpeakHours = "";
            result.contractInfo.segment = webPrmInfo.segment;
            result.contractInfo.subscribedPower = "" + webPrmInfo.puissanceSouscrite;

            result.prmId = prmId;
            result.customerId = "";

            return result;

        }
    }

    public String formatUrl(String apiUrl, String prmId) {
        return apiUrl.formatted(prmId);
    }

    public Customer getCustomer(LinkyHandler handler, String prmId) throws LinkyException {
        if (!apiBridgeHandler.isConnected()) {
            apiBridgeHandler.initialize();
        }

        String contractUrl = apiBridgeHandler.getContractUrl();
        String data = getData(handler, formatUrl(contractUrl, prmId));
        if (data.isEmpty()) {
            throw new LinkyException("Requesting '%s' returned an empty response", contractUrl);
        }
        try {
            CustomerReponse cResponse = gson.fromJson(data, CustomerReponse.class);
            if (cResponse == null) {
                throw new LinkyException("Invalid customer data received");
            }
            return cResponse.customer;
        } catch (JsonSyntaxException e) {
            logger.debug("invalid JSON response not matching PrmInfo[].class: {}", data);
            throw new LinkyException(e, "Requesting '%s' returned an invalid JSON response", contractUrl);
        }
    }

    public AddressInfo getAddress(LinkyHandler handler, String prmId) throws LinkyException {
        if (!apiBridgeHandler.isConnected()) {
            apiBridgeHandler.initialize();
        }
        String addressUrl = apiBridgeHandler.getAddressUrl();
        String data = getData(handler, formatUrl(addressUrl, prmId));
        if (data.isEmpty()) {
            throw new LinkyException("Requesting '%s' returned an empty response", addressUrl);
        }
        try {
            CustomerReponse cResponse = gson.fromJson(data, CustomerReponse.class);
            if (cResponse == null) {
                throw new LinkyException("Invalid customer data received");
            }
            return cResponse.customer.usagePoints[0].usagePoint.usagePointAddresses;
        } catch (JsonSyntaxException e) {
            logger.debug("invalid JSON response not matching PrmInfo[].class: {}", data);
            throw new LinkyException(e, "Requesting '%s' returned an invalid JSON response", addressUrl);
        }
    }

    public IdentityInfo getIdentity(LinkyHandler handler, String prmId) throws LinkyException {
        if (!apiBridgeHandler.isConnected()) {
            apiBridgeHandler.initialize();
        }
        String identityUrl = apiBridgeHandler.getIdentityUrl();
        String data = getData(handler, formatUrl(identityUrl, prmId));
        if (data.isEmpty()) {
            throw new LinkyException("Requesting '%s' returned an empty response", identityUrl);
        }
        try {
            CustomerIdResponse iResponse = gson.fromJson(data, CustomerIdResponse.class);
            if (iResponse == null) {
                throw new LinkyException("Invalid customer data received");
            }
            return iResponse.identity.naturalPerson;
        } catch (JsonSyntaxException e) {
            logger.debug("invalid JSON response not matching PrmInfo[].class: {}", data);
            throw new LinkyException(e, "Requesting '%s' returned an invalid JSON response", identityUrl);
        }
    }

    public ContactInfo getContact(LinkyHandler handler, String prmId) throws LinkyException {
        if (!apiBridgeHandler.isConnected()) {
            apiBridgeHandler.initialize();
        }
        String contactUrl = apiBridgeHandler.getContactUrl();
        String data = getData(handler, formatUrl(contactUrl, prmId));

        if (data.isEmpty()) {
            throw new LinkyException("Requesting '%s' returned an empty response", contactUrl);
        }
        try {
            CustomerIdResponse cResponse = gson.fromJson(data, CustomerIdResponse.class);
            if (cResponse == null) {
                throw new LinkyException("Invalid customer data received");
            }
            return cResponse.contactData;
        } catch (JsonSyntaxException e) {
            logger.debug("invalid JSON response not matching PrmInfo[].class: {}", data);
            throw new LinkyException(e, "Requesting '%s' returned an invalid JSON response", contactUrl);
        }
    }

    private MeterReading getMeasures(LinkyHandler handler, String apiUrl, String prmId, LocalDate from, LocalDate to)
            throws LinkyException {
        String dtStart = from.format(API_DATE_FORMAT);
        String dtEnd = to.format(API_DATE_FORMAT);

        String url = String.format(apiUrl, prmId, dtStart, dtEnd);
        if (!apiBridgeHandler.isConnected()) {
            apiBridgeHandler.initialize();
        }

        String data = getData(handler, url);
        if (data.isEmpty()) {
            throw new LinkyException("Requesting '%s' returned an empty response", url);
        }
        logger.trace("getData returned {}", data);
        try {
            MeterResponse meterResponse = gson.fromJson(data, MeterResponse.class);
            if (meterResponse == null) {
                throw new LinkyException("No report data received");
            }
            return meterResponse.meterReading;
        } catch (JsonSyntaxException e) {
            logger.debug("invalid JSON response not matching ConsumptionReport.class: {}", data);
            throw new LinkyException(e, "Requesting '%s' returned an invalid JSON response", url);
        }
    }

    public MeterReading getEnergyData(LinkyHandler handler, String prmId, LocalDate from, LocalDate to)
            throws LinkyException {
        return getMeasures(handler, apiBridgeHandler.getDailyConsumptionUrl(), prmId, from, to);
    }

    public MeterReading getPowerData(LinkyHandler handler, String prmId, LocalDate from, LocalDate to)
            throws LinkyException {
        return getMeasures(handler, apiBridgeHandler.getMaxPowerUrl(), prmId, from, to);
    }

    public TempoResponse getTempoData(LinkyHandler handler) throws LinkyException {
        String url = String.format(apiBridgeHandler.getTempoUrl(), "2024-01-01", "2024-08-30");

        if (url.isEmpty()) {
            return new TempoResponse();
        }

        if (!apiBridgeHandler.isConnected()) {
            apiBridgeHandler.initialize();
        }

        String data = getData(handler, url);
        if (data.isEmpty()) {
            throw new LinkyException("Requesting '%s' returned an empty response", url);
        }
        logger.trace("getData returned {}", data);

        try {
            TempoResponse tempResponse = gson.fromJson(data, TempoResponse.class);
            if (tempResponse == null) {
                throw new LinkyException("No report data received");
            }

            return tempResponse;
        } catch (JsonSyntaxException e) {
            logger.debug("invalid JSON response not matching ConsumptionReport.class: {}", data);
            throw new LinkyException(e, "Requesting '%s' returned an invalid JSON response", url);
        }

        // return data;
    }
}
