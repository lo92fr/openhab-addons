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
package org.openhab.binding.smartthings.internal.converter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.smartthings.internal.type.SmartthingsTypeRegistry;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converter class for Smartthings "Color" capability and not the "Color Control" capability.
 * The Smartthings Color capability seems to be a later capability where the hue is in the standard 0 - 360 range and
 * therefore doesn't need to be converted for openHAB
 *
 * @author Bob Raker - Initial contribution
 */
@NonNullByDefault
public class SmartthingsColorConverter extends SmartthingsConverter {

    private Pattern rgbInputPattern = Pattern.compile("^#[0-9a-fA-F]{6}");

    private final Logger logger = LoggerFactory.getLogger(SmartthingsColorConverter.class);

    public SmartthingsColorConverter(SmartthingsTypeRegistry typeRegistry, Thing thing) {
        super(typeRegistry, thing);
    }

    @Override
    public String convertToSmartthings(Thing thing, ChannelUID channelUid, Command command) {
        return defaultConvertToSmartthings(thing, channelUid, command);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.openhab.binding.smartthings.internal.converter.SmartthingsConverter#convertToOpenHab(java.lang.String,
     * org.openhab.binding.smartthings.internal.SmartthingsStateData)
     */
    @Override
    public State convertToOpenHab(Thing thing, ChannelUID channelUid, Object dataFromSmartthings) {
        // The color value from Smartthings will look like "#123456" which is the RGB color
        // This needs to be converted into HSB type
        if (dataFromSmartthings == null) {
            logger.warn("Failed to convert color because Smartthings returned a null value.");
            return UnDefType.UNDEF;
        }

        String value = (String) dataFromSmartthings;
        // First verify the format the string is valid
        Matcher matcher = rgbInputPattern.matcher(value);
        if (!matcher.matches()) {
            logger.warn(
                    "The \"value\" in the following message is not a valid color. Expected a value like \"#123456\" instead of {}",
                    dataFromSmartthings.toString());
            return UnDefType.UNDEF;
        }

        // Get the RGB colors
        int[] rgb = new int[3];
        for (int i = 0, pos = 1; i < 3; i++, pos += 2) {
            String c = value.substring(pos, pos + 2);
            rgb[i] = Integer.parseInt(c, 16);
        }

        // Convert to state
        return HSBType.fromRGB(rgb[0], rgb[1], rgb[2]);
    }
}
