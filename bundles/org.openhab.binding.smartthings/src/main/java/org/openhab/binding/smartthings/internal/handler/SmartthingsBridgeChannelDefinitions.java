/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.smartthings.internal.handler;

import java.util.Hashtable;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smartthings.internal.SmartthingsBindingConstants;
import org.openhab.core.semantics.SemanticTag;
import org.openhab.core.semantics.model.DefaultSemanticTags.Point;
import org.openhab.core.semantics.model.DefaultSemanticTags.Property;

/**
 * Configuration data for Smartthings hub
 *
 * @author Bob Raker - Initial contribution
 */
@NonNullByDefault
public class SmartthingsBridgeChannelDefinitions {
    private static final SmartthingsBridgeChannelDefinitions INSTANCE = new SmartthingsBridgeChannelDefinitions();

    private final Hashtable<String, String> channelTypes = new Hashtable<String, String>();
    private final Hashtable<String, ChannelProperty> channelProperties = new Hashtable<String, ChannelProperty>();

    public static @Nullable String getChannelType(String key) {
        return SmartthingsBridgeChannelDefinitions.INSTANCE.getChannelTypeInternal(key);
    }

    private @Nullable String getChannelTypeInternal(String key) {
        if (channelTypes.containsKey(key)) {
            return channelTypes.get(key);
        }

        return null;
    }

    public class ChannelProperty {
        private @Nullable String openhabChannelType;
        private @Nullable String uOm;
        private @Nullable SemanticTag semanticPoint;
        private @Nullable SemanticTag semanticProperty;

        public ChannelProperty(String openhabChannelType) {
            this.openhabChannelType = openhabChannelType;
            this.uOm = null;
        }

        public ChannelProperty(@Nullable String openhabChannelType, @Nullable String uOm) {
            this.openhabChannelType = openhabChannelType;
            this.uOm = uOm;
        }

        public ChannelProperty(@Nullable String openhabChannelType, @Nullable String uOm,
                @Nullable SemanticTag semanticPoint, @Nullable SemanticTag semanticProperty) {
            this.openhabChannelType = openhabChannelType;
            this.uOm = uOm;
            this.semanticPoint = semanticPoint;
            this.semanticProperty = semanticProperty;
        }

        public ChannelProperty(@Nullable SemanticTag semanticPoint, @Nullable SemanticTag semanticProperty) {
            this.openhabChannelType = null;
            this.uOm = null;
            this.semanticPoint = semanticPoint;
            this.semanticProperty = semanticProperty;
        }

        public @Nullable String getOpenhabChannelType() {
            return this.openhabChannelType;
        }

        public @Nullable String getUoM() {
            return this.uOm;
        }

        public @Nullable SemanticTag getSemanticPoint() {
            return this.semanticPoint;
        }

        public @Nullable SemanticTag getSemanticProperty() {
            return this.semanticProperty;
        }
    }

    public static @Nullable ChannelProperty getChannelProperty(String key) {
        return SmartthingsBridgeChannelDefinitions.INSTANCE.getChannelPropertyInternal(key);
    }

    private @Nullable ChannelProperty getChannelPropertyInternal(String key) {
        if (channelProperties.containsKey(key)) {
            return channelProperties.get(key);
        }

        return null;
    }

    public SmartthingsBridgeChannelDefinitions() {
        // powerMeter
        channelProperties.put("powerMeter#power",
                new ChannelProperty(null, "Power", Point.MEASUREMENT, Property.POWER));

        // energyMeter
        channelProperties.put("energyMeter#energy",
                new ChannelProperty(null, "Energy", Point.MEASUREMENT, Property.ENERGY));

        // waterSensor
        channelProperties.put("waterSensor#water", new ChannelProperty(Point.MEASUREMENT, Property.HUMIDITY));

        // battery
        channelProperties.put("battery#battery",
                new ChannelProperty(null, "Dimensionless", Point.STATUS, Property.LEVEL));

        channelProperties.put("legendabsolute60149.signalMetrics#signalMetrics",
                new ChannelProperty(null, "Percent", Point.MEASUREMENT, Property.SIGNAL_STRENGTH));

        // switch
        channelProperties.put("switch#switch",
                new ChannelProperty(SmartthingsBindingConstants.TYPE_SWITCH, null, Point.SWITCH, Property.LIGHT));

        // colorControl
        channelProperties.put("colorControl#saturation",
                new ChannelProperty(SmartthingsBindingConstants.TYPE_NUMBER, null, Point.CONTROL, Property.COLOR));
        channelProperties.put("colorControl#hue",
                new ChannelProperty(SmartthingsBindingConstants.TYPE_NUMBER, null, Point.CONTROL, Property.COLOR));
        channelProperties.put("colorControl#color",
                new ChannelProperty(SmartthingsBindingConstants.TYPE_COLOR, null, Point.CONTROL, Property.COLOR));

        // colorTemperature
        channelProperties.put("colorTemperature#colorTemperature",
                new ChannelProperty(SmartthingsBindingConstants.TYPE_NUMBER, null, Point.CONTROL, null));

        // switchLevel
        channelProperties.put("switchLevel#level", new ChannelProperty(SmartthingsBindingConstants.TYPE_DIMMER,
                "Dimensionless", Point.CONTROL, Property.BRIGHTNESS));

        // base type
        channelTypes.put(SmartthingsBindingConstants.SM_TYPE_INTEGER, SmartthingsBindingConstants.TYPE_NUMBER);
        channelTypes.put(SmartthingsBindingConstants.SM_TYPE_STRING, SmartthingsBindingConstants.TYPE_STRING);
        channelTypes.put(SmartthingsBindingConstants.SM_TYPE_OBJECT, SmartthingsBindingConstants.TYPE_STRING);
        channelTypes.put(SmartthingsBindingConstants.SM_TYPE_ARRAY, SmartthingsBindingConstants.TYPE_STRING);
        channelTypes.put(SmartthingsBindingConstants.SM_TYPE_NUMBER, SmartthingsBindingConstants.TYPE_NUMBER);
        channelTypes.put(SmartthingsBindingConstants.SM_TYPE_BOOLEAN, SmartthingsBindingConstants.TYPE_CONTACT);
    }
}

/*
 *
 * Acceleration Meter per square second (m/s²) Meter per square second (m/s²)
 * Amount of Substance Mole (mol) Mole (mol)
 * Angle Degree (°) Degree (°)
 * Area Square Meter (m²) Square foot (ft²)
 * Areal Density Dobson unit (DU) Dobson unit (DU)
 * Catalytic Activity Katal (kat) Katal (kat)
 * Data Amount Byte (B) Byte (B)
 * Data Transfer Rate Megabit per second (Mbit/s) Megabit per second (Mbit/s)
 * Density Kilogram per cubic meter (kg/m³) Kilogram per cubic meter (kg/m³)
 * Dimensionless Abstract unit one (one) Abstract unit one (one)
 * Electric Capacitance Farad (F) Farad (F)
 * Electric Charge Coulomb (C) Coulomb (C)
 * Electric Conductance Siemens (S) Siemens (S)
 * Electric Conductivity Siemens per meter (S/m) Siemens per meter (S/m)
 * Electric Current Ampere (A) Ampere (A)
 * Electric Inductance Henry (H) Henry (H)
 * Electric Potential Volt (V) Volt (V)
 * Electric Resistance Ohm (Ω) Ohm (Ω)
 * Emission Intensity Gram per kilowatt hour (g/kWh) Gram per kilowatt hour (g/kWh)
 * Force Newton (N) Newton (N)
 * Frequency Hertz (Hz) Hertz (Hz)
 * Illuminance Lux (lx) Lux (lx)
 * Intensity Irradiance (W/m²) Irradiance (W/m²)
 * Length Meter (m) Inch (in)
 * Luminous Flux Lumen (lm) Lumen (lm)
 * Luminous Intensity Candela (cd) Candela (cd)
 * Magnetic Flux Weber (Wb) Weber (Wb)
 * Magnetic Flux Density Tesla (T) Tesla (T)
 * Mass Kilogram (kg) Pound (lb)
 * Pressure Hectopascal (hPa) Inch of mercury (inHg)
 * Radiant Exposure Joule per square meter (J/m²) Joule per square meter (J/m²)
 * Radiation Absorbed Dose Gray (Gy) Gray (Gy)
 * Radiation Effective Dose Sievert (Sv) Sievert (Sv)
 * Radioactivity Becquerel (Bq) Becquerel (Bq)
 * Solid Angle Steradian (sr) Steradian (sr)
 * Speed Kilometers per hour (km/h) Miles per hour (mph)
 * Temperature Celsius (°C) Fahrenheit (°F)
 * Time Seconds (s) Seconds (s)
 * Volume Cubic Meter (m³) US Gallon (gal)
 * Volumetric Flow Rate Liter per minute (l/min) US Gallon per minute (gal/min)
 */
