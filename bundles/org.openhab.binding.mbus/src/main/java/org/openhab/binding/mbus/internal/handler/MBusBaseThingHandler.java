/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.mbus.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mbus.internal.discovery.MBusDeviceDiscoveryService;
import org.openhab.binding.mbus.internal.network.MBusConnector;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MBusBaseThingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Laurent Arnal - Initial contribution and API
 */
@NonNullByDefault
public abstract class MBusBaseThingHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(MBusBaseThingHandler.class);
    protected @Nullable MBusTCPBridgeHandler bridgeHandler;
    private @Nullable MBusDeviceDiscoveryService discoveryService;
    protected @Nullable MBusConnector connector;

    public MBusBaseThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Reference
    public void setMBusConnector(@Nullable MBusConnector mBusConnector) {
        this.connector = mBusConnector;
    }

    public void unsetSiemensHvacConnector(MBusConnector hvacConnector) {
        this.connector = null;
    }

    @Override
    public void initialize() {
        MBusTCPBridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler == null) {
            logger.debug("Initializing '{}': thing is only supported within a bridge", getDescription());
            updateStatus(ThingStatus.OFFLINE);
            return;
        }
        logger.trace("Initializing '{}' thing", getDescription());
        bridgeHandler.registerThing(this);
    }

    protected synchronized @Nullable MBusTCPBridgeHandler getBridgeHandler() {
        if (this.bridgeHandler == null) {
            Bridge bridge = getBridge();
            if (bridge == null) {
                return null;
            }
            ThingHandler handler = bridge.getHandler();
            if (handler instanceof MBusTCPBridgeHandler) {
                this.bridgeHandler = (MBusTCPBridgeHandler) handler;
            }
        }
        return this.bridgeHandler;
    }

    /**
     * return an internal description for logging
     *
     * @return the description of the thing
     */
    protected abstract String getDescription();

    @Override
    public void updateStatus(ThingStatus status) {
        super.updateStatus(status);
    }

    @Override
    public void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
        super.updateStatus(status, statusDetail, description);
    }

    public boolean registerDiscoveryListener(MBusDeviceDiscoveryService listener) {
        if (discoveryService == null) {
            discoveryService = listener;
            return true;
        }
        return false;
    }

    public boolean unregisterDiscoveryListener() {
        if (discoveryService != null) {
            discoveryService = null;
            return true;
        }

        return false;
    }
}
