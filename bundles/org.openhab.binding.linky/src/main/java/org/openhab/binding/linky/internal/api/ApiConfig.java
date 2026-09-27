package org.openhab.binding.linky.internal.api;

import java.time.format.DateTimeFormatter;

public class ApiConfig {
    protected String enedisDomain = ".enedis.fr";

    protected String baseUrl;
    protected String accountUrl;

    protected String userName = "";
    protected String password = "";
    protected String internalAuthId = "";

    protected String tokenUrl = "";
    protected String authorizeUrl = "";
    protected String clientId = "";
    protected String clientSecret = "";

    protected String contractUrl;
    protected String identityUrl;
    protected String contactUrl;
    protected String adressUrl;

    protected String userInfoContractUrl;

    protected String dailyConsumptionUrl;
    protected String dailyIndexUrl;
    protected String loadCurveUrl;
    protected String maxPowerUrl;
    protected String tempoUrl;

    protected String subscribedServiceUrl;
    protected String contractSyntheseUrl;
    protected String alimentationUrl;

    protected String urlMonCompte;
    protected String urlMonCompteParticulier;
    protected String urlEnedisAuthenticate;

    protected DateTimeFormatter apiDateFormat;
    protected DateTimeFormatter apiDateFormatYearsFirst;
    protected double divider;

    protected boolean useOAuth;

    public ApiConfig() {
        baseUrl = "";
        accountUrl = "";
        contractUrl = "";
        identityUrl = "";
        contactUrl = "";
        adressUrl = "";
        userInfoContractUrl = "";
        dailyConsumptionUrl = "";
        dailyIndexUrl = "";
        loadCurveUrl = "";
        maxPowerUrl = "";
        tempoUrl = "";
        subscribedServiceUrl = "";
        contractSyntheseUrl = "";
        alimentationUrl = "";
        urlMonCompte = "";
        urlMonCompteParticulier = "";
        urlEnedisAuthenticate = "";
        divider = 1000;
        useOAuth = false;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setInternalAuthId(String internalAuthId) {
        this.internalAuthId = internalAuthId;
    }

    public double getDivider() {
        return divider;
    }

    public boolean useOAuth() {
        return useOAuth;
    }
}
