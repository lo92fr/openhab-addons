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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.smartthings.internal.type.SmartthingsTypeRegistry;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converter class for Smartthings capability "Color Control".
 * The Smartthings "Color Control" capability represents the hue values in the 0-100% range. OH2 uses 0-360 degrees
 * For this converter only the hue is coming into openHAB and it is a number
 *
 * @author Bob Raker - Initial contribution
 */
@NonNullByDefault
public class SmartthingsHue100Converter extends SmartthingsConverter {

    private final Logger logger = LoggerFactory.getLogger(SmartthingsHue100Converter.class);

    public SmartthingsHue100Converter(SmartthingsTypeRegistry typeRegistry) {
        super(typeRegistry);
    }

    @Override
    public void convertToSmartthingsInternal(Thing thing, ChannelUID channelUid, Command command) {
        String jsonMsg;

        if (command instanceof HSBType hsbCommand) {
            double hue = hsbCommand.getHue().doubleValue() / 3.60;

            String componentKey = "main";
            String capaKey = "colorControl";
            String cmdName = "setHue";
            Object[] arguments = new Object[1];
            arguments[0] = hue;

            this.pushCommand(componentKey, capaKey, cmdName, arguments);
        } else if (command instanceof DecimalType dec) {
            double hue = dec.doubleValue() / 3.60;

            String componentKey = "main";
            String capaKey = "colorControl";
            String cmdName = "setHue";
            Object[] arguments = new Object[1];
            arguments[0] = hue;

            this.pushCommand(componentKey, capaKey, cmdName, arguments);
        } else {
            logger.info("");
        }

    }

    // <!-- The Smartthings colorControl:hue has a range of 0-100% where OH2 uses the normal 0-360 degrees -->
    @Override
    public State convertToOpenHabInternal(Thing thing, ChannelUID channelUid, Object dataFromSmartthings) {
        // Here we have to multiply the value from Smartthings by 3.6 to convert from 0-100 to 0-360

        if (dataFromSmartthings instanceof Double value) {
            value *= 3.6;

            return new DecimalType(value);
        } else if (dataFromSmartthings instanceof String value) {
            double d = Double.parseDouble(value);
            d *= 3.6;

            return new DecimalType(d);
        }

        return UnDefType.UNDEF;

        /*
         * if (deviceValue instanceof String stringCommand) {
         * double d = Double.parseDouble(stringCommand);
         * d *= 3.6;
         * return new DecimalType(d);
         * } else if (deviceValue instanceof Long) {
         * double d = ((Long) deviceValue).longValue();
         * d *= 3.6;
         * return new DecimalType(d);
         * } else if (deviceValue instanceof BigDecimal decimalValue) {
         * double d = decimalValue.doubleValue();
         * d *= 3.6;
         * return new DecimalType(d);
         * } else if (deviceValue instanceof Number numberValue) {
         * double d = numberValue.doubleValue();
         * d *= 3.6;
         * return new DecimalType(d);
         * }
         */
    }
}
