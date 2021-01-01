package org.openhab.binding.siemenshvac.internal.network;

import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public interface SiemensHvacConnector {

    public JsonObject DoRequest(@Nullable String req);

    public Gson getGson();
}
