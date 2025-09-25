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
package org.openhab.binding.smartthings.internal.stateHandler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.smartthings.internal.handler.SmartthingsThingHandler;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * Base converter class.
 * The converter classes are responsible for converting "state" messages from the smartthings hub into openHAB States.
 * And, converting handler.handleCommand() into messages to be sent to smartthings
 *
 * @author Bob Raker - Initial contribution
 */
@NonNullByDefault
public class SmartthingsStateHandlerLight extends SmartthingsStateHandler {

    private final Logger logger = LoggerFactory.getLogger(SmartthingsStateHandlerLight.class);

    private Gson gson = new Gson();

    SmartthingsStateHandlerLight() {
    }

    @Override
    public void handleStateChange(ChannelUID channelUID, State state, SmartthingsThingHandler thingHandler) {
        State oldHueState = stateCache.get("hue");
        State oldSaturationState = stateCache.get("saturation");
        State oldLevelState = stateCache.get("level");

        if (oldHueState == null) {
            oldHueState = new DecimalType(0);
        }

        if (oldSaturationState == null) {
            oldSaturationState = new PercentType(0);
        }

        if (oldLevelState == null) {
            oldLevelState = new PercentType(0);
        }

        String groupId = channelUID.getGroupId();
        if (groupId == null) {
            groupId = "main";
        }

        ChannelUID channelUIDColor = new ChannelUID(thingHandler.getThing().getUID(), groupId, "color");
        if (channelUID.getIdWithoutGroup().equals("hue")) {
            stateCache.put("hue", state);
            HSBType newColorState = new HSBType((DecimalType) state, convToPercentTypeIfNeed(oldSaturationState),
                    (PercentType) oldLevelState);

            thingHandler.sendUpdateState(channelUIDColor, newColorState);
        }
        if (channelUID.getIdWithoutGroup().equals("saturation")) {
            stateCache.put("saturation", state);
            HSBType newColorState = new HSBType((DecimalType) oldHueState, convToPercentTypeIfNeed(state),
                    (PercentType) oldLevelState);

            thingHandler.sendUpdateState(channelUIDColor, newColorState);
        }
        if (channelUID.getIdWithoutGroup().equals("level")) {
            stateCache.put("level", state);
            HSBType newColorState = new HSBType((DecimalType) oldHueState, convToPercentTypeIfNeed(oldSaturationState),
                    (PercentType) state);

            thingHandler.sendUpdateState(channelUIDColor, newColorState);
        }
    }

}
