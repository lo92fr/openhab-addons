/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.siemenshvac.internal.type;

import org.openhab.binding.siemenshvac.internal.constants.SiemensHvacBindingConstants;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.type.ChannelGroupTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * Utility class for generating some UIDs.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class UidUtils {

    /**
     * Generates the ThingTypeUID for the given device. If it's a Homegear device, add a prefix because a Homegear
     * device has more datapoints.
     */
    public static ThingTypeUID generateThingTypeUID(String name) {
        // if (!device.isGatewayExtras() && device.getGatewayId().equals(HmGatewayInfo.ID_HOMEGEAR)) {
        // return new ThingTypeUID(SiemensHvacBindingConstants.BINDING_ID, "String.format("HG-%s", device.getType())");
        // } else {
        return new ThingTypeUID(SiemensHvacBindingConstants.BINDING_ID, "deviceType");
        // }
    }

    /**
     * Generates the ChannelTypeUID for the given datapoint with deviceType, channelNumber and datapointName.
     */
    public static ChannelTypeUID generateChannelTypeUID(int i) {
        return new ChannelTypeUID(SiemensHvacBindingConstants.BINDING_ID, "chanid" + i);
        // "String.format("%s_%s_%s", dp.getChannel().getDevice().getType(),
        // dp.getChannel().getNumber(), dp.getName())");
    }

    /**
     * Generates the ChannelTypeUID for the given datapoint with deviceType and channelNumber.
     */
    public static ChannelGroupTypeUID generateChannelGroupTypeUID(int i) {
        return new ChannelGroupTypeUID(SiemensHvacBindingConstants.BINDING_ID, "chan" + i);

        // String.format("%s_%s", channel.getDevice().getType(), channel.getNumber()));
    }

    /**
     * Generates the ThingUID for the given device in the given bridge.
     */
    public static ThingUID generateThingUID(Bridge bridge) {
        ThingTypeUID thingTypeUID = generateThingTypeUID("");
        return new ThingUID(thingTypeUID, bridge.getUID(), "");
    }

    /**
     * Generates the ChannelUID for the given datapoint with channelNumber and datapointName.
     */
    public static ChannelUID generateChannelUID(ThingUID thingUID) {
        return new ChannelUID(thingUID, "");
        // String.valueOf(dp.getChannel().getNumber()), dp.getName());
    }

}
