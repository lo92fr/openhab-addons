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
package org.openhab.binding.mbus.internal.discovery;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mbus.internal.constants.MBusBindingConstants;
import org.openhab.binding.mbus.internal.handler.MBusTCPBridgeHandler;
import org.openhab.binding.mbus.internal.network.MBusConnector;
import org.openhab.binding.mbus.internal.network.MBusConnectorImpl;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openmuc.jmbus.SecondaryAddress;
import org.openmuc.jmbus.VariableDataStructure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MBusDeviceDiscoveryService} tracks for Mbus device connected to the bus.
 *
 * @author Laurent Arnal - Initial contribution
 */

public class MBusDeviceDiscoveryService extends AbstractDiscoveryService
        implements DiscoveryService, ThingHandlerService {

    private static final Logger logger = LoggerFactory.getLogger(MBusDeviceDiscoveryService.class);

    private @Nullable MBusTCPBridgeHandler mBusBridgeHandler;
    private @Nullable MBusConnector mbusConnector;
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections
            .singleton(MBusBindingConstants.THING_TYPE_MBUSTCPGATEWAY);

    private static final int SEARCH_TIME = 10;

    public MBusDeviceDiscoveryService() {
        super(SUPPORTED_THING_TYPES, SEARCH_TIME);
    }

    @Override
    protected void startBackgroundDiscovery() {
    }

    @Override
    protected void stopBackgroundDiscovery() {
        // can be overridden
    }

    private @Nullable ThingUID getThingUID(ThingTypeUID thingTypeUID, String serial) {
        if (mBusBridgeHandler != null) {
            ThingUID localBridgeUID = mBusBridgeHandler.getThing().getUID();
            if (localBridgeUID != null) {
                return new ThingUID(thingTypeUID, localBridgeUID, serial);
            }
        }
        return null;
    }

    @Override
    public void startScan() {
        logger.debug("call startScan()");

        final MBusTCPBridgeHandler handler = mBusBridgeHandler;
        MBusConnectorImpl cnx = (MBusConnectorImpl) mBusBridgeHandler.getMBusConnector();

        for (int idx = 0; idx < 5; idx++) {
            logger.info("Search devices: " + idx);
            logger.info("Search devices: " + idx);

            cnx.resetSlave(idx);

            VariableDataStructure result = cnx.readSlave(idx);

            if (result != null) {
                logger.info("Find devices: " + idx);

                SecondaryAddress sAddr = result.getSecondaryAddress();
                String manufacturer = sAddr.getManufacturerId();
                String deviceType = sAddr.getDeviceType().toString();
                int version = sAddr.getVersion();
                String deviceId = sAddr.getDeviceId().toString();

                ThingTypeUID thingTypeUID = new ThingTypeUID(MBusBindingConstants.BINDING_ID, "mbuscounter");

                ThingUID thingUID = getThingUID(thingTypeUID, manufacturer + "_" + idx);
                ThingUID bridgeUID = mBusBridgeHandler.getThing().getUID();

                if (thingUID != null) {
                    Map<String, Object> properties = new HashMap<>(1);
                    properties.put("name", manufacturer + "_" + idx);
                    properties.put("manufacturer", manufacturer);
                    properties.put("version", version);
                    properties.put("primaryAddr", idx);
                    properties.put("type", deviceType);
                    properties.put("addr", result.getSecondaryAddress());
                    properties.put("serialNr", deviceId);

                    DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                            .withBridge(bridgeUID).withLabel("mbus_" + idx).build();
                    thingDiscovered(discoveryResult);
                }
            }
        }
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof MBusTCPBridgeHandler) {
            mBusBridgeHandler = (MBusTCPBridgeHandler) handler;
            // bridgeUID = handler.getThing().getUID();
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return mBusBridgeHandler;
    }

    @Override
    public void activate() {
        final MBusTCPBridgeHandler handler = mBusBridgeHandler;
        if (handler != null) {
            handler.registerDiscoveryListener(this);
        }
    }

    @Override
    public void deactivate() {
        /*
         * removeOlderResults(new Date().getTime(), bridgeUID);
         * final HueBridgeHandler handler = hueBridgeHandler;
         * if (handler != null) {
         * handler.unregisterDiscoveryListener();
         * }
         */
    }
}
