package org.openhab.binding.linky.internal.api;

import java.time.format.DateTimeFormatter;

public class ApiConfigEnedisApi extends ApiConfig {

    public ApiConfigEnedisApi(boolean isSandBox) {

        if (isSandBox) {
            baseUrl = "https://gw.ext.prod-sandbox.api.enedis.fr/";
            accountUrl = "https://gw.ext.prod-sandbox.api.enedis.fr";
        } else {
            baseUrl = "https://gw.ext.prod.api.enedis.fr/";
            accountUrl = "https://mon-compte-particulier.enedis.fr/";
        }

        urlMonCompte = "";
        urlMonCompteParticulier = "";
        urlEnedisAuthenticate = "";
        userInfoContractUrl = "";

        contractUrl = baseUrl + "customers_upc/v5/usage_points/contracts?usage_point_id=%s";
        identityUrl = baseUrl + "customers_i/v5/identity?usage_point_id=%s";
        contactUrl = baseUrl + "customers_cd/v5/contact_data?usage_point_id=%s";
        adressUrl = baseUrl + "customers_upa/v5/usage_points/addresses?usage_point_id=%s";

        dailyConsumptionUrl = baseUrl + "metering_data_dc/v5/daily_consumption?usage_point_id=%s&start=%s&end=%s";
        dailyIndexUrl = baseUrl + "metering_data_dc/v5/daily_consumption?usage_point_id=%s&start=%s&end=%s";
        loadCurveUrl = baseUrl + "metering_data_clc/v5/consumption_load_curve?usage_point_id=%s&start=%s&end=%s";
        maxPowerUrl = baseUrl + "metering_data_dcmp/v5/daily_consumption_max_power?usage_point_id=%s&start=%s&end=%s";

        tempoUrl = "https://www.myelectricaldata.fr/" + "rte/tempo/%s/%s";

        subscribedServiceUrl = "";
        contractSyntheseUrl = "";
        alimentationUrl = "";

        apiDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        apiDateFormatYearsFirst = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        divider = 1000;

        tokenUrl = baseUrl + "oauth2/v3/token";
        authorizeUrl = accountUrl + "dataconnect/v1/oauth2/authorize?duration=P36M";

        useOAuth = true;

    }
}
