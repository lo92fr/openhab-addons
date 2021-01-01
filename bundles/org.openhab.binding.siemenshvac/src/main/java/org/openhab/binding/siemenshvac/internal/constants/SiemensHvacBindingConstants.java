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
package org.openhab.binding.siemenshvac.internal.constants;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link SiemensHvacBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Laurent ARNAL - Initial contribution
 */
@NonNullByDefault
public class SiemensHvacBindingConstants {

    public static final String BINDING_ID = "siemenshvac";

    public static final String CONFIG_DESCRIPTION_URI_CHANNEL = "channel-type:siemenshvac:config";

    // List of all Thing Type UIDs
    // Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_OZW672 = new ThingTypeUID(BINDING_ID, "ozw672");
    public static final ThingTypeUID THING_TYPE_DEVICE = new ThingTypeUID(BINDING_ID, "device");

    // List of all Channel ids
    public static final String CHANNEL_1 = "channel1";

    public static final String IP_ADDRESS = "ipAddress";

    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");

    public static final String PROPERTY_VENDOR_NAME = "eQ-3 AG";

    public static final String ITEM_TYPE_SWITCH = "Switch";
    public static final String ITEM_TYPE_ROLLERSHUTTER = "Rollershutter";
    public static final String ITEM_TYPE_CONTACT = "Contact";
    public static final String ITEM_TYPE_STRING = "String";
    public static final String ITEM_TYPE_NUMBER = "Number";
    public static final String ITEM_TYPE_DIMMER = "Dimmer";
    public static final String ITEM_TYPE_DATETIME = "DateTime";

    public static final String CONFIG_DESCRIPTION_URI_THING_PREFIX = "thing-type";

    public static final String CHANNEL_TYPE_DUTY_CYCLE_RATIO = "DUTY_CYCLE_RATIO";

    public static final String CATEGORY_BATTERY = "Battery";
    public static final String CATEGORY_ALARM = "Alarm";
    public static final String CATEGORY_HUMIDITY = "Humidity";
    public static final String CATEGORY_TEMPERATURE = "Temperature";
    public static final String CATEGORY_MOTION = "Motion";
    public static final String CATEGORY_PRESSURE = "Pressure";
    public static final String CATEGORY_SMOKE = "Smoke";
    public static final String CATEGORY_WATER = "Water";
    public static final String CATEGORY_WIND = "Wind";
    public static final String CATEGORY_RAIN = "Rain";
    public static final String CATEGORY_ENERGY = "Energy";
    public static final String CATEGORY_BLINDS = "Blinds";
    public static final String CATEGORY_CONTACT = "Contact";
    public static final String CATEGORY_SWITCH = "Switch";

    public static final String DATAPOINT_NAME_CONFIG_PENDING = "CONFIG_PENDING";
    public static final String DATAPOINT_NAME_UPDATE_PENDING = "UPDATE_PENDING";
    public static final String DATAPOINT_NAME_UNREACH = "UNREACH";
    public static final String DATAPOINT_NAME_DEVICE_IN_BOOTLOADER = "DEVICE_IN_BOOTLOADER";
    public static final String DATAPOINT_NAME_INSTALL_TEST = "INSTALL_TEST";
    public static final String DATAPOINT_NAME_BATTERY_TYPE = "BATTERY_TYPE";
    public static final String DATAPOINT_NAME_LOWBAT = "LOWBAT";
    public static final String DATAPOINT_NAME_STATE = "STATE";
    public static final String DATAPOINT_NAME_HUMIDITY = "HUMIDITY";
    public static final String DATAPOINT_NAME_TEMPERATURE = "TEMPERATURE";
    public static final String DATAPOINT_NAME_MOTION = "MOTION";
    public static final String DATAPOINT_NAME_AIR_PRESSURE = "AIR_PRESSURE";
    public static final String DATAPOINT_NAME_WIND_SPEED = "WIND_SPEED";
    public static final String DATAPOINT_NAME_RAIN = "RAIN";
    public static final String DATAPOINT_NAME_BOOT = "BOOT";
    public static final String DATAPOINT_NAME_FREQUENCY = "FREQUENCY";
    public static final String DATAPOINT_NAME_SENSOR = "SENSOR";
    public static final String DATAPOINT_NAME_LEVEL = "LEVEL";
    public static final String DATAPOINT_NAME_SUBMIT = "SUBMIT";
    public static final String DATAPOINT_NAME_BEEP = "BEEP";
    public static final String DATAPOINT_NAME_BACKLIGHT = "BACKLIGHT";
    public static final String DATAPOINT_NAME_UNIT = "UNIT";
    public static final String DATAPOINT_NAME_TEXT = "TEXT";
    public static final String DATAPOINT_NAME_ON_TIME = "ON_TIME";
    public static final String DATAPOINT_NAME_STOP = "STOP";
    public static final String DATAPOINT_NAME_RSSI_DEVICE = "RSSI_DEVICE";
    public static final String DATAPOINT_NAME_RSSI_PEER = "RSSI_PEER";
    public static final String DATAPOINT_NAME_AES_KEY = "AES_KEY";
    public static final String DATAPOINT_NAME_VALUE = "VALUE";
    public static final String DATAPOINT_NAME_CALIBRATION = "CALIBRATION";
    public static final String DATAPOINT_NAME_LOWBAT_IP = "LOW_BAT";
    public static final String DATAPOINT_NAME_CHANNEL_FUNCTION = "CHANNEL_FUNCTION";

    public static final String VIRTUAL_DATAPOINT_NAME_BATTERY_TYPE = "BATTERY_TYPE";
    public static final String VIRTUAL_DATAPOINT_NAME_DELETE_DEVICE_MODE = "DELETE_DEVICE_MODE";
    public static final String VIRTUAL_DATAPOINT_NAME_DELETE_DEVICE = "DELETE_DEVICE";
    public static final String VIRTUAL_DATAPOINT_NAME_DISPLAY_OPTIONS = "DISPLAY_OPTIONS";
    public static final String VIRTUAL_DATAPOINT_NAME_FIRMWARE = "FIRMWARE";
    public static final String VIRTUAL_DATAPOINT_NAME_INSTALL_MODE = "INSTALL_MODE";
    public static final String VIRTUAL_DATAPOINT_NAME_INSTALL_MODE_DURATION = "INSTALL_MODE_DURATION";
    public static final String VIRTUAL_DATAPOINT_NAME_ON_TIME_AUTOMATIC = "ON_TIME_AUTOMATIC";
    public static final String VIRTUAL_DATAPOINT_NAME_RELOAD_ALL_FROM_GATEWAY = "RELOAD_ALL_FROM_GATEWAY";
    public static final String VIRTUAL_DATAPOINT_NAME_RELOAD_FROM_GATEWAY = "RELOAD_FROM_GATEWAY";
    public static final String VIRTUAL_DATAPOINT_NAME_RELOAD_RSSI = "RELOAD_RSSI";
    public static final String VIRTUAL_DATAPOINT_NAME_RSSI = "RSSI";
    public static final String VIRTUAL_DATAPOINT_NAME_STATE_CONTACT = "STATE_CONTACT";
    public static final String VIRTUAL_DATAPOINT_NAME_SIGNAL_STRENGTH = "SIGNAL_STRENGTH";
    public static final String VIRTUAL_DATAPOINT_NAME_BUTTON = "BUTTON";

}
