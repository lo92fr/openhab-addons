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
package org.openhab.binding.siemenshvac.internal.factory;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.siemenshvac.internal.Metadata.SiemensHvacMetadataRegistry;
import org.openhab.binding.siemenshvac.internal.constants.SiemensHvacBindingConstants;
import org.openhab.binding.siemenshvac.internal.handler.SiemensHvacHandler;
import org.openhab.binding.siemenshvac.internal.handler.SiemensHvacOZW672BridgeThingHandler;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.net.http.HttpClientFactory;
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
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.siemenshvac")
public class SiemensHvacHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(SiemensHvacHandlerFactory.class);

    public static final Collection<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Arrays
            .asList(SiemensHvacBindingConstants.THING_TYPE_DEVICE, SiemensHvacBindingConstants.THING_TYPE_OZW672);

    private @Nullable NetworkAddressService networkAddressService;
    private @Nullable HttpClientFactory httpClientFactory;
    private @Nullable SiemensHvacMetadataRegistry metaDataRegistry;

    //

    @Activate
    public SiemensHvacHandlerFactory(final @Reference HttpClientFactory httpClientFactory,
            final @Reference SiemensHvacMetadataRegistry metaDataRegistry) {
        this.httpClientFactory = httpClientFactory;
        this.metaDataRegistry = metaDataRegistry;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        boolean result = SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);

        if (result) {
            logger.debug("p1");
        }
        return result;
    }

    @Override
    public @Nullable Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration,
            @Nullable ThingUID thingUID, @Nullable ThingUID bridgeUID) {
        if (SiemensHvacBindingConstants.THING_TYPE_OZW672.equals(thingTypeUID)) {
            ThingUID IPBridgeUID = getIPBridgeThingUID(thingTypeUID, thingUID, configuration);
            return super.createThing(thingTypeUID, configuration, IPBridgeUID, null);
        }
        if (SiemensHvacBindingConstants.THING_TYPE_DEVICE.equals(thingTypeUID)) {
            return super.createThing(thingTypeUID, configuration, thingUID, bridgeUID);
        }
        throw new IllegalArgumentException("The thing type " + thingTypeUID + " is not supported by the KNX binding.");
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        if (thing.getThingTypeUID().equals(SiemensHvacBindingConstants.THING_TYPE_OZW672)) {
            return new SiemensHvacOZW672BridgeThingHandler((Bridge) thing, networkAddressService, httpClientFactory,
                    metaDataRegistry);
        } else if (thing.getThingTypeUID().equals(SiemensHvacBindingConstants.THING_TYPE_DEVICE)) {
            return new SiemensHvacHandler(thing);
        }
        return null;
    }

    private ThingUID getIPBridgeThingUID(ThingTypeUID thingTypeUID, @Nullable ThingUID thingUID,
            Configuration configuration) {
        if (thingUID != null) {
            return thingUID;
        }
        String ipAddress = (String) configuration.get(SiemensHvacBindingConstants.IP_ADDRESS);
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
