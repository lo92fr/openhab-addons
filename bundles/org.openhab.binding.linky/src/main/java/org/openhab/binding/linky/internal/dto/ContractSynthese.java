package org.openhab.binding.linky.internal.dto;

import com.google.gson.annotations.SerializedName;

public class ContractSynthese {

    public Segment[] segments;

    public class Segment {
        public String segment;
    }

    @SerializedName("generation_last_activation_date")
    public String generationLastActivationDate;

    @SerializedName("consumption_last_activation_date")
    public String consumptionLastActivationDate;

    @SerializedName("last_subscribed_power_change_date")
    public String lastSubscribedPowerChangeDate;

}
