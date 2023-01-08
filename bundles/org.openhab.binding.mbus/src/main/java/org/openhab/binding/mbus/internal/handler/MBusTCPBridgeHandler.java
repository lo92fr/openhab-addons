/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import java.util.Collection;
import java.util.Collections;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mbus.internal.discovery.MBusDeviceDiscoveryService;
import org.openhab.binding.mbus.internal.network.MBusConnector;
import org.openhab.binding.mbus.internal.network.MBusConnectorImpl;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.osgi.service.component.annotations.Activate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link IPBridgeThingHandler} is responsible for handling commands, which are
 * sent to one of the channels. It implements a KNX/IP Gateway, that either acts a a
 * conduit for other {@link DeviceThingHandler}s, or for Channels that are
 * directly defined on the bridge
 *
 * @author Karel Goderis - Initial contribution
 * @author Simon Kaufmann - Refactoring & cleanup
 */
@NonNullByDefault
public class MBusTCPBridgeHandler extends MBusBridgeBaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(MBusTCPBridgeHandler.class);
    private @Nullable MBusConnector mBusConnector;

    @Activate
    public MBusTCPBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        logger.debug("Initialize() bridge");

        mBusConnector = new MBusConnectorImpl();
        updateStatus(ThingStatus.ONLINE);
    }

    public @Nullable MBusConnector getMBusConnector() {
        return mBusConnector;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void dispose() {
        super.dispose();
        /*
         * if (client != null) {
         * client.dispose();
         * client = null;
         * }
         */
    }

    public void registerThing(final MBusBaseThingHandler parameter) {
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(MBusDeviceDiscoveryService.class);
    }
}
