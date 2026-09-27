package org.openhab.binding.linky.internal.dto;

import com.google.gson.annotations.SerializedName;

public class Address {
    @SerializedName("number_street_name")
    public String numberStreetName;

    @SerializedName("postal_code_city")
    public String postalCodeCity;

    @SerializedName("insee_code")
    public String insee_code;

    @SerializedName("line4")
    public String line4;

    @SerializedName("line6")
    public String line6;
}