package org.openhab.binding.linky.internal.api;

import java.time.format.DateTimeFormatter;

public class ApiConfigMyElectrical extends ApiConfig {

    public ApiConfigMyElectrical() {
        baseUrl = "https://www.myelectricaldata.fr/";
        accountUrl = "https://www.myelectricaldata.fr/";

        urlMonCompte = "";
        urlMonCompteParticulier = "";
        urlEnedisAuthenticate = "";
        userInfoContractUrl = "";

        contractUrl = baseUrl + "contracts/%s/cache/";
        identityUrl = baseUrl + "identity/%s/cache/";
        contactUrl = baseUrl + "contact/%s/cache/";
        adressUrl = baseUrl + "addresses/%s/cache/";

        dailyConsumptionUrl = baseUrl + "daily_consumption/%s/start/%s/end/%s/cache";
        dailyIndexUrl = baseUrl + "daily_consumption/%s/start/%s/end/%s/cache";
        loadCurveUrl = baseUrl + "consumption_load_curve/%s/start/%s/end/%s/cache";
        maxPowerUrl = baseUrl + "daily_consumption_max_power/%s/start/%s/end/%s/cache";

        tempoUrl = baseUrl + "rte/tempo/%s/%s";

        subscribedServiceUrl = "";
        contractSyntheseUrl = "";
        alimentationUrl = "";

        apiDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        apiDateFormatYearsFirst = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        divider = 1000;

        tokenUrl = accountUrl
                + "v1/oauth2/authorize?client_id=%s&response_type=code&redirect_uri=na&user_type=na&state=na&person_id=-1&usage_points_id=%s";

        authorizeUrl = "";

        clientId = "_h7zLaRr2INxqBI8jhDUQXsa_G4a";

        useOAuth = true;
    }
}
