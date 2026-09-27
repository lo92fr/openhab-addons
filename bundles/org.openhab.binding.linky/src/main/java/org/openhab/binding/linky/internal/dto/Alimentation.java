package org.openhab.binding.linky.internal.dto;

import com.google.gson.annotations.SerializedName;

public class Alimentation {

    @SerializedName("serial_number")
    public String serialNumber;

    @SerializedName("connection_state")
    public String connectionState;

    @SerializedName("voltage_level")
    public String voltageLevel;

    @SerializedName("consumption_connection_power")
    public Power consumptionConnectionPower;

    @SerializedName("generation_connection_power")
    public Power generationConnectionPower;

    @SerializedName("nominal_service_voltage")
    public Power nominalServiceVoltage;

}
