package org.openhab.binding.linky.internal.api;

import java.time.format.DateTimeFormatter;

public class ApiConfigEnedisWeb extends ApiConfig {

    public ApiConfigEnedisWeb() {
        baseUrl = "https://alex.microapplications.enedis.fr";
        accountUrl = "";
        String prmInfoBaseUrl = baseUrl + "/mes-mesures-prm/api/private/v2/personnes/";

        contractUrl = baseUrl + "/mes-prms-part/api/private/v2/personnes/%s/prms";
        identityUrl = baseUrl + "/userinfos";
        contactUrl = baseUrl + "/userinfos";
        adressUrl = "";

        urlMonCompte = "https://mon-compte.enedis.fr";
        urlMonCompteParticulier = "https://mon-compte-particulier.enedis.fr";
        urlEnedisAuthenticate = baseUrl + "/authenticate?target=" + urlMonCompteParticulier;
        userInfoContractUrl = baseUrl + "/mon-compte/api/private/v2/userinfos";

        dailyConsumptionUrl = prmInfoBaseUrl
                + "%s/prms/%s/donnees-energetiques?mesuresTypeCode=ENERGIE&mesuresCorrigees=false&typeDonnees=CONS&segments=%s";
        dailyIndexUrl = prmInfoBaseUrl
                + "%s/prms/%s/donnees-energetiques?mesuresTypeCode=INDEX&mesuresCorrigees=false&typeDonnees=CONS&segments=%s";
        loadCurveUrl = prmInfoBaseUrl
                + "%s/prms/%s/donnees-energetiques?mesuresTypeCode=COURBE&mesuresCorrigees=false&typeDonnees=CONS&segments=%s&dateDebut=%s";
        maxPowerUrl = prmInfoBaseUrl
                + "%s/prms/%s/donnees-energetiques?mesuresTypeCode=PMAX&mesuresCorrigees=false&typeDonnees=CONS&segments=%s";

        tempoUrl = "https://www.myelectricaldata.fr/rte/tempo/%s/%s";

        subscribedServiceUrl = "";
        contractSyntheseUrl = "";
        alimentationUrl = "";

        apiDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        apiDateFormatYearsFirst = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        divider = 1;
    }
}
