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
package org.openhab.binding.mbus.internal.factory;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mbus.internal.constants.MBusBindingConstants;
import org.openhab.binding.mbus.internal.handler.MBusCounterHandlerImpl;
import org.openhab.binding.mbus.internal.handler.MBusTCPBridgeHandler;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SiemensHvacHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Laurent ARNAL - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.mbus")
public class MBusHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(MBusHandlerFactory.class);

    private @Nullable NetworkAddressService networkAddressService;
    private @Nullable MBusTCPBridgeHandler bridge;

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set
            .of(MBusBindingConstants.THING_TYPE_MBUSTCPGATEWAY, MBusBindingConstants.THING_TYPE_MBUSCOUNTER);

    //

    @Activate
    public MBusHandlerFactory() {
        logger.info("in MBusHandlerFactory");
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    public @Nullable Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration,
            @Nullable ThingUID thingUID, @Nullable ThingUID bridgeUID) {

        if (MBusBindingConstants.THING_TYPE_MBUSTCPGATEWAY.equals(thingTypeUID)) {
            ThingUID IPBridgeUID = getIPBridgeThingUID(thingTypeUID, thingUID, configuration);
            return super.createThing(thingTypeUID, configuration, IPBridgeUID, null);
        } else if (MBusBindingConstants.THING_TYPE_MBUSCOUNTER.equals(thingTypeUID)) {
            return super.createThing(thingTypeUID, configuration, thingUID, bridgeUID);
        }
        throw new IllegalArgumentException("The thing type " + thingTypeUID + " is not supported by the KNX binding.");
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(MBusBindingConstants.THING_TYPE_MBUSTCPGATEWAY)) {
            if (networkAddressService != null) {
                bridge = new MBusTCPBridgeHandler((Bridge) thing);
                return bridge;
            }
        } else if (thingTypeUID.equals(MBusBindingConstants.THING_TYPE_MBUSCOUNTER)) {
            MBusCounterHandlerImpl handler = new MBusCounterHandlerImpl(thing);

            if (bridge != null) {
                handler.setMBusConnector(bridge.getMBusConnector());
            }

            return handler;
        }
        return null;
    }

    private ThingUID getIPBridgeThingUID(ThingTypeUID thingTypeUID, @Nullable ThingUID thingUID,
            Configuration configuration) {
        if (thingUID != null) {
            return thingUID;
        }
        String ipAddress = (String) configuration.get(MBusBindingConstants.IP_ADDRESS);
        return new ThingUID(thingTypeUID, ipAddress);
    }

    @Reference
    protected void setNetworkAddressService(NetworkAddressService networkAddressService) {
        this.networkAddressService = networkAddressService;
    }

    protected void unsetNetworkAddressService(NetworkAddressService networkAddressService) {
        this.networkAddressService = null;
    }
}
