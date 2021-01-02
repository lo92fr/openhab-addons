package org.openhab.binding.siemenshvac.internal.network;

import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.siemenshvac.internal.Metadata.RuntimeTypeAdapterFactory;
import org.openhab.binding.siemenshvac.internal.Metadata.SiemensHvacMetadata;
import org.openhab.binding.siemenshvac.internal.Metadata.SiemensHvacMetadataDataPoint;
import org.openhab.binding.siemenshvac.internal.Metadata.SiemensHvacMetadataMenu;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Component(immediate = true)
@NonNullByDefault
public class SiemensHvacConnectorImpl implements SiemensHvacConnector {

    private static final Logger logger = LoggerFactory.getLogger(SiemensHvacConnectorImpl.class);

    private @Nullable String sessionId = null;
    private String baseUrl = "";
    private String userName = "";
    private String userPassword = "";
    private @Nullable Date lastUpdate;

    protected final HttpClientFactory httpClientFactory;

    protected @Nullable HttpClient httpClient;
    protected @Nullable HttpClient httpClientInsecure;

    private static int startedRequest = 0;
    private static int completedRequest = 0;
    private Lock lockObj = new ReentrantLock();

    // private siemensMetadataRegistry registry = new siemensMetadataRegistry(this);

    // private Map<String, Type> updateCommand;

    // private boolean interrupted = false;

    // private siemensHvacBinding hvacBinding;

    // siemensHvacBinding hvacBinding

    @Activate
    public SiemensHvacConnectorImpl(@Reference HttpClientFactory httpClientFactory) {
        // this.hvacBinding = hvacBinding;
        // this.updateCommand = new Hashtable<String, Type>();
        this.httpClientFactory = httpClientFactory;

        initHttpClient();

    }

    private void initHttpClient() {

        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.httpClientInsecure = new HttpClient(new SslContextFactory.Client(true));
        this.httpClientInsecure.setRemoveIdleDestinations(true);
        this.httpClientInsecure.setMaxConnectionsPerDestination(15);
        try {
            this.httpClientInsecure.start();
        } catch (Exception e) {
            // catching exception is necessary due to the signature of HttpClient.start()
            logger.warn("Failed to start insecure http client: {}", e.getMessage());
        }

    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    @Override
    public void onComplete(@Nullable Request request) {
        lockObj.lock();
        try {
            completedRequest++;
        } finally {
            lockObj.unlock();
        }
        logger.debug("unregisterCount:" + completedRequest + " " + request.getURI());
    }

    private @Nullable ContentResponse executeRequest(final Request request, @Nullable SiemensHvacCallback callback)
            throws Exception {
        request.timeout(60, TimeUnit.SECONDS);

        ContentResponse response = null;

        @Nullable
        SiemensHvacRequestListener requestListener = null;
        if (callback != null) {
            requestListener = new SiemensHvacRequestListener(callback, this);
            request.onResponseSuccess(requestListener);
            request.onResponseFailure(requestListener);
        }

        try {
            if (requestListener != null) {
                lockObj.lock();
                try {
                    startedRequest++;
                } finally {
                    lockObj.unlock();
                }

                logger.debug("registerCount:" + startedRequest + " " + request.getQuery());
                request.send(requestListener);
            } else {
                response = request.send();
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new Exception("siemensHvac:Exception by executing request: " + request.getQuery() + " ; "
                    + e.getLocalizedMessage());
        }
        return response;
    }

    private void _doAuth() {
        logger.debug("siemensHvac:doAuth()");

        userName = "Administrator";
        userPassword = "Drf67tio1!";

        String baseUri = "https://192.168.254.53/";
        String uri = "api/auth/login.json?user=" + userName + "&pwd=" + userPassword;
        final Request request = httpClientInsecure.newRequest(baseUri + uri);
        request.method(HttpMethod.GET);

        logger.debug("siemensHvac:doAuth:connect()");

        try {
            ContentResponse response = executeRequest(request, null);
            int statusCode = response.getStatus();

            logger.debug("siemensHvac:doAuth:Endresponse:()" + statusCode);

            if (statusCode == HttpStatus.OK_200) {
                String result = response.getContentAsString();

                if (result != null) {
                    logger.debug("siemensHvac:doAuth:decodeResponse:()" + result);
                    JsonObject resultObj = getGson().fromJson(result, JsonObject.class);

                    if (resultObj.has("Result")) {
                        JsonElement resultVal = resultObj.get("Result");
                        JsonObject resultObj2 = resultVal.getAsJsonObject();

                        if (resultObj2.has("Success")) {
                            boolean successVal = resultObj2.get("Success").getAsBoolean();

                            if (successVal) {

                                if (resultObj.has("SessionId")) {
                                    sessionId = resultObj.get("SessionId").getAsString();
                                    logger.debug("Have new SessionId : " + sessionId);
                                }

                            }

                        }
                    }

                    logger.debug("siemensHvac:doAuth:decodeResponse:()");

                }

                if (sessionId == null) {
                    logger.debug("Session request auth was unsucessfull in _doAuth()");
                }
            }

            logger.debug("siemensHvac:doAuth:connect()");

        } catch (Exception ex) {
            logger.debug("siemensHvac:doAuth:error()" + ex.getLocalizedMessage());
        } finally {
        }
    }

    public @Nullable String DoBasicRequest(@Nullable String uri) {
        return DoBasicRequest(uri, null);
    }

    public @Nullable String DoBasicRequestAsync(@Nullable String uri, @Nullable SiemensHvacCallback callback) {
        return DoBasicRequest(uri, callback);
    }

    public @Nullable String DoBasicRequest(@Nullable String uri, @Nullable SiemensHvacCallback callback) {
        if (sessionId == null) {
            _doAuth();
        }

        try {
            String baseUri = "https://192.168.254.53/";
            if (!uri.endsWith("?")) {
                uri = uri + "&";
            }
            uri = uri + "SessionId=" + sessionId;
            uri = uri + "&user=" + userName + "&pwd=" + userPassword;

            final Request request = httpClientInsecure.newRequest(baseUri + uri);
            request.method(HttpMethod.GET);

            logger.debug("siemensHvac:DoBasicRequest()" + request);

            ContentResponse response = executeRequest(request, callback);
            if (callback == null) {
                int statusCode = response.getStatus();

                if (statusCode == HttpStatus.OK_200) {
                    String result = response.getContentAsString();

                    return result;
                }
            }
        } catch (Exception ex) {
            logger.error(
                    "siemensHvac:DoRequest:Exception by executing Request: " + uri + " ; " + ex.getLocalizedMessage());
        } finally {
        }

        return null;
    }

    @Override
    public @Nullable JsonObject DoRequest(@Nullable String req, @Nullable SiemensHvacCallback callback) {
        try {
            String response = DoBasicRequest(req, callback);

            if (response != null) {
                // logger.debug("siemensHvacDoRequest:responseSt:" + response);

                JsonObject resultObj = getGson().fromJson(response, JsonObject.class);

                if (resultObj.has("Result")) {
                    JsonObject subResultObj = resultObj.getAsJsonObject("Result");

                    if (subResultObj.has("Success")) {
                        boolean result = subResultObj.get("Success").getAsBoolean();
                        if (result) {
                            return resultObj;
                        }
                    }

                }

                return null;
            }
        } catch (Exception e) {
            logger.error("siemensHvac:DoRequest:Exception by executing jsonRequest: " + req + " ; "
                    + e.getLocalizedMessage());
        }

        return null;
    }

    @Override
    public void WaitAllPendingRequest() {
        logger.debug("WaitAllPendingRequest:start");
        try {
            logger.debug("WaitAllPendingRequest:start Initial Sleep");
            Thread.sleep(1000);
            logger.debug("WaitAllPendingRequest:end Initial Sleep");

            logger.debug("WaitAllPendingRequest:start Wait Request");
            boolean allRequestDone = false;

            while (!allRequestDone) {
                int idx = 0;

                allRequestDone = true;
                while (idx < 5 && allRequestDone) {
                    logger.debug("WaitAllPendingRequest:waitAllRequestDone " + idx + " " + startedRequest + "/"
                            + completedRequest);
                    if (startedRequest != completedRequest) {
                        allRequestDone = false;
                    }
                    Thread.sleep(1000);
                    idx++;
                }
            }

            logger.debug("WaitAllPendingRequest:end Wait");

        } catch (InterruptedException ex) {
            logger.debug("WaitAllPendingRequest:interrupted in WaitAllRequest");
        }

        logger.debug("WaitAllPendingRequest:end Wait");
        logger.debug("WaitAllPendingRequest:end WaitAllPendingRequest");
    }

    public static Gson getGson() {
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.setPrettyPrinting().create();
        return gson;
    }

    public static Gson getGsonWithAdapter() {
        RuntimeTypeAdapterFactory<SiemensHvacMetadata> adapter = RuntimeTypeAdapterFactory
                .of(SiemensHvacMetadata.class);
        adapter.registerSubtype(SiemensHvacMetadataMenu.class);
        adapter.registerSubtype(SiemensHvacMetadataDataPoint.class);

        Gson gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapterFactory(adapter).create();
        return gson;
    }

    /*
     * siemensHvacGenericBindingProvider provider = hvacBinding.getProvider();
     *
     * if (provider.getBindingConfigs().values().isEmpty()) {
     * return;
     * }
     *
     * try {
     * registry.Init();
     *
     * logger.debug("siemensHvac:readAllDP():begin");
     *
     * Iterator<BindingConfig> it = provider.getBindingConfigs().values().iterator();
     *
     * while (it.hasNext()) {
     * siemensHvacBindingConfig config = (siemensHvacBindingConfig) it.next();
     * String name = config.getName();
     * registry.VerifyBindingConfig(config);
     * String type = config.getDptType();
     * int dp = config.getDptId();
     *
     * ReadDp(name, dp, type);
     * }
     * } catch (Exception ex) {
     * logger.error("siemensHvac:ReadAllDp: " + ex.getLocalizedMessage());
     * throw ex;
     * }
     * logger.debug("siemensHvac:readAllDP():end");
     *
     * }
     */

    /*
     * public String ReadDp(String name, int dp, String type) {
     * String value = _readDpInternal(name, dp, type);
     *
     * logger.debug("siemensHvac:ReadDP:" + name + ":" + dp + ":" + value);
     *
     * if (value == null || value.equals("----") || value.equals("")) {
     * return null;
     * }
     *
     * if (type == null) {
     * logger.debug("siemensHvac:ReadDP:null type" + name + ":" + dp);
     * return "Err";
     * }
     * if (type.equals("Numeric")) {
     * hvacBinding.getEventPublisher().postUpdate(name, new DecimalType(value));
     * } else if (type.equals("Enumeration")) {
     * String valueEnum = value;
     * String valueText = value;
     * String[] values = value.split(":");
     * valueEnum = values[0];
     * valueText = values[1];
     *
     * hvacBinding.getEventPublisher().postUpdate(name, new StringType(value));
     * } else if (type.equals("Text")) {
     * hvacBinding.getEventPublisher().postUpdate(name, new StringType(value));
     * } else {
     * hvacBinding.getEventPublisher().postUpdate(name, new StringType(value));
     * }
     *
     * return value;
     * }
     */

    /*
     * public void WriteDp(String name, Type dp) {
     * siemensHvacGenericBindingProvider provider = hvacBinding.getProvider();
     * siemensHvacBindingConfig bindingConfig = (siemensHvacBindingConfig) provider.getBindingConfigs().get(name);
     * registry.VerifyBindingConfig(bindingConfig);
     * int dpt = bindingConfig.getDptId();
     * String type = bindingConfig.getDptType();
     *
     * String valAct = _readDpInternal(name, dpt, type);
     * String valActEnum = valAct;
     * String valActLabel = valAct;
     *
     * String valUpdateEnum = "";
     * String valUpdateLabel = "";
     *
     * if (type.equals("Enumeration")) {
     * String[] values = valAct.split(":");
     * valActEnum = values[0];
     * valActLabel = values[1];
     * }
     *
     * String valUpdate = "";
     *
     * if (dp instanceof PercentType) {
     * PercentType pct = (PercentType) dp;
     * valUpdate = pct.toString();
     * } else if (dp instanceof DecimalType) {
     * DecimalType bdc = (DecimalType) dp;
     * valUpdate = bdc.toString();
     * } else if (dp instanceof StringType) {
     * StringType bdc = (StringType) dp;
     * valUpdate = bdc.toString();
     *
     * if (type.equals("Enumeration")) {
     * String[] valuesUpdateDp = valUpdate.split(":");
     * valUpdateEnum = valuesUpdateDp[0];
     * valUpdateLabel = valuesUpdateDp[1];
     *
     * // For enumeration, we always update using the raw value
     * valUpdate = valUpdateEnum;
     * }
     * }
     *
     * // Exit there if new value is the same as old value
     * if (valAct != null && valUpdate.equals(valAct)) {
     * return;
     * }
     * if (valActEnum != null && valUpdateEnum.equals(valActEnum)) {
     * return;
     * }
     *
     * siemensHvacBindingConfig config = (siemensHvacBindingConfig) provider.getBindingConfigs().get(name);
     * registry.VerifyBindingConfig(bindingConfig);
     * String dpType = config.getDptType();
     *
     * String request = "api/menutree/write_datapoint.json?Id=" + dpt + "&Value=" + valUpdate + "&Type=" + dpType;
     *
     * JSONObject result = DoRequest(request);
     * logger.debug("siemensHvac:WriteDP(response) = " + result);
     *
     * ReadDp(name, dpt, type);
     * }
     */

    /*
     * private String _readDpInternal(String name, int dp, String type) {
     * if (dp == -1) {
     * return "";
     * }
     *
     * try {
     * String request = "api/menutree/read_datapoint.json?Id=" + dp;
     *
     * // logger.debug("siemensHvac:ReadDp:DoRequest():" + request);
     *
     * JSONObject result = DoRequest(request);
     *
     * if (result != null && result.containsKey("Data")) {
     * JSONObject subResult = (JSONObject) result.get("Data");
     *
     * // {"Value":"Automatique","Type":"Enumeration","EnumValue":"1","Unit":""}
     * String typer = "";
     * String value = "";
     * String enumValue = "";
     *
     * if (subResult.containsKey("Type")) {
     * typer = subResult.get("Type").toString().trim();
     * }
     * if (subResult.containsKey("Value")) {
     * value = subResult.get("Value").toString().trim();
     * }
     * if (subResult.containsKey("EnumValue")) {
     * enumValue = subResult.get("EnumValue").toString().trim();
     * }
     *
     * if (typer.equals("Enumeration")) {
     * return "" + enumValue + ":" + value;
     * } else {
     * return value;
     * }
     * } else {
     * sessionId = null;
     * }
     * } catch (Exception e) {
     * logger.error("siemensHvac:ReadDp:Error during dp reading: " + name + " ; " + e.getLocalizedMessage());
     * // Reset sessionId so we redone _auth on error
     * sessionId = null;
     * }
     *
     * return "";
     * }
     */
    /*
     * public void AddDpUpdate(String itemName, Type dp) {
     * synchronized (updateCommand) {
     * updateCommand.put(itemName, dp);
     * lastUpdate = new java.util.Date();
     * }
     * }
     */

    /*
     * @Override
     * public void run() {
     * logger.debug("siemensHvac:sender thread start");
     *
     * // as long as no interrupt is requested, continue running
     * while (!interrupted) {
     * try {
     * Thread.sleep(2000);
     * Date currentDate = new java.util.Date();
     *
     * logger.debug("siemensHvac:sender thread alive:" + currentDate);
     *
     * if (lastUpdate == null) {
     * continue;
     * }
     *
     * long ms = currentDate.getTime() - lastUpdate.getTime();
     * if (ms < 3000) {
     * continue;
     * }
     *
     * synchronized (updateCommand) {
     * if (updateCommand.isEmpty()) {
     * continue;
     * }
     *
     * logger.debug("siemensHvac:sender thread updateCommand");
     *
     * for (String key : updateCommand.keySet()) {
     * Type dp = updateCommand.get(key);
     * WriteDp(key, dp);
     *
     * }
     *
     * updateCommand.clear();
     * }
     *
     * } catch (Exception e) {
     * logger.error("siemensHvac:Error occured will sending update values", e);
     * }
     * }
     *
     * }
     */
}
