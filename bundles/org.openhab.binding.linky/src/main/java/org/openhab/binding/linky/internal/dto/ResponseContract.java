/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.linky.internal.dto;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link ResponseContract} holds informations about the contract
 *
 * @author Gaël L'hopital - Initial contribution
 * @author Laurent Arnal - Rewrite addon to use official dataconect API
 */

public class ResponseContract {
    @SerializedName("usage_point_id")
    public String usagePoint;

    @SerializedName("contract_start")
    public String contractStart;

    @SerializedName("contract_type")
    public String contractType;

    public String contractor;

    @SerializedName("pricing_structure")
    public String pricingStructure;

    @SerializedName("distribution_tariff")
    public String distributionTarriff;

    @SerializedName("distribution_tariff_profile")
    public DistributionTariffProfile[] distributionTariffProfile;

    public class DistributionTariffProfile {
        public String name;
        public Power power;

    }

    @SerializedName("subscribed_power")
    public Power subscribedPower;

    public String segment;

    public Customer customer;

    public class Customer {
        public Customer customer;
        public Address adress;
    }

    public Organization organization;

    public class Organization {
        public String name;

        @SerializedName("business_code")
        public String businessCode;

        @SerializedName("siret_number")
        public String siretNumber;

        @SerializedName("siren_number")
        public String sirenNumber;
    }
}
