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
package org.openhab.binding.siemenshvac.internal.handler;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.siemenshvac.internal.config.SiemensHvacConfiguration;
import org.openhab.binding.siemenshvac.internal.network.SiemensHvacCallback;
import org.openhab.binding.siemenshvac.internal.network.SiemensHvacConnector;
import org.openhab.binding.siemenshvac.internal.type.SiemensHvacChannelGroupTypeProvider;
import org.openhab.binding.siemenshvac.internal.type.SiemensHvacChannelTypeProvider;
import org.openhab.binding.siemenshvac.internal.type.SiemensHvacConfigDescriptionProvider;
import org.openhab.binding.siemenshvac.internal.type.SiemensHvacThingTypeProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * The {@link SiemensHvacHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Laurent ARNAL - Initial contribution
 */
@Component(immediate = true)
@NonNullByDefault
public class SiemensHvacHandlerImpl extends BaseThingHandler implements SiemensHvacHandler {

    private final Logger logger = LoggerFactory.getLogger(SiemensHvacHandlerImpl.class);

    private @Nullable ScheduledFuture<?> pollingJob = null;

    private @Nullable SiemensHvacConfiguration config;
    private @Nullable SiemensHvacThingTypeProvider thingTypeProvider;
    private @Nullable SiemensHvacChannelTypeProvider channelTypeProvider;
    private @Nullable SiemensHvacChannelGroupTypeProvider channelGroupTypeProvider;
    private @Nullable SiemensHvacConfigDescriptionProvider configDescriptionProvider;
    private @Nullable SiemensHvacConnector hvacConnector;

    public SiemensHvacHandlerImpl(Thing thing) {
        super(thing);

        logger.debug("===========================================================");
        logger.debug("Siemens HVac");
        logger.debug("===========================================================");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand");
        if (command instanceof RefreshType) {
            logger.debug("handleCommand1");
        }
    }

    @Reference
    public void setSiemensHvacConnector(@Nullable SiemensHvacConnector hvacConnector) {
        this.hvacConnector = hvacConnector;
    }

    public void unsetSiemensHvacConnector(SiemensHvacConnector hvacConnector) {
        this.hvacConnector = null;
    }

    @Reference
    public void setChannelTypeProvider(@Nullable SiemensHvacChannelTypeProvider channelTypeProvider) {
        this.channelTypeProvider = channelTypeProvider;
    }

    public void unsetChannelTypeProvider(@Nullable SiemensHvacChannelTypeProvider channelTypeProvider) {
        this.channelTypeProvider = null;
    }

    @Override
    public void initialize() {

        // TODO: Initialize the handler.
        // The framework requires you to return from this method quickly. Also, before leaving this method a thing
        // case you can decide it directly.
        // In case you can not decide the thing status directly (e.g. for long running connection handshake using WAN
        // access or similar) you should set status UNKNOWN here and then decide the real status asynchronously in the
        // background.

        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        // the framework is then able to reuse the resources from the thing handler initialization.
        // we set this upfront to reliably check status updates in unit tests.
        updateStatus(ThingStatus.UNKNOWN);

        // Example for background initialization:
        scheduler.execute(() -> {
            boolean thingReachable = true; // <background task with long running initialization here>
            // when done do:
            if (thingReachable) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
        });

        config = getConfigAs(SiemensHvacConfiguration.class);
        var c1 = getThing().getConfiguration();
        var c2 = getBridge().getConfiguration();

        // These logging types should be primarily used by bindings
        // logger.trace("Example trace message");
        // logger.debug("Example debug message");
        // logger.warn("Example warn message");

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");

        pollingJob = scheduler.scheduleWithFixedDelay(this::pollingCode, 0, 10, TimeUnit.SECONDS);
    }

    private void pollingCode() {

        var chList = this.getThing().getChannels();
        for (Channel ch : chList) {
            logger.debug(ch.getDescription());

            boolean isLink = this.getCallback().isChannelLinked(ch.getUID());

            if (!isLink) {
                continue;
            }

            if (channelTypeProvider == null) {
                return;
            }

            ChannelType tp = channelTypeProvider.getInternalChannelType(ch.getChannelTypeUID());

            String dptId = ch.getProperties().get("dptId");
            String groupId = ch.getProperties().get("groupdId");
            String label = ch.getLabel();
            String uid = ch.getUID().getId();

            ReadDp(groupId, dptId, label, uid, "");
            logger.debug("" + isLink);
        }
    }

    public void DecodeReadDp(JsonObject response, @Nullable String groupId, @Nullable String dp, @Nullable String uid,
            @Nullable String label, String type) {
        if (response != null && response.has("Data")) {
            JsonObject subResult = (JsonObject) response.get("Data");

            String updateKey = "" + label;
            String typer = "";
            String value = "";
            String enumValue = "";
            String result = "";

            if (dp.equals("1506")) {
                type = "Numeric";
            }

            if (subResult.has("Type")) {
                typer = subResult.get("Type").toString().trim();
            }
            if (subResult.has("Value")) {
                value = "" + subResult.get("Value").getAsDouble();
            }
            if (subResult.has("EnumValue")) {
                enumValue = subResult.get("EnumValue").toString().trim();
            }

            if (typer.equals("Enumeration")) {
                result = "" + enumValue + ":" + value;
            } else {
                result = value;
            }

            if (value == null || value.equals("----") || value.equals("")) {
                return;
            }

            if (type == null) {
                logger.debug("siemensHvac:ReadDP:null type" + dp);
                return;
            }
            if (type.equals("Numeric")) {
                updateState(updateKey, new DecimalType(value));
            } else if (type.equals("Enumeration")) {
                String valueEnum = value;
                String valueText = value;
                String[] values = value.split(":");
                valueEnum = values[0];
                valueText = values[1];

                updateState(updateKey, new StringType(value));
            } else if (type.equals("Text")) {
                updateState(updateKey, new StringType(value));
            } else {
                updateState(updateKey, new DateTimeType());
            }

        }
    }

    private void ReadDp(@Nullable String groupId, @Nullable String dp, @Nullable String label, @Nullable String uid,
            String type) {
        if (dp.equals("-1")) {
            return;
        }

        try {
            String request = "api/menutree/read_datapoint.json?Id=" + dp;

            // logger.debug("siemensHvac:ReadDp:DoRequest():" + request);

            hvacConnector.DoRequest(request, new SiemensHvacCallback() {

                @Override
                public void execute(java.net.URI uri, int status, @Nullable Object response) {
                    if (response instanceof JsonObject) {
                        DecodeReadDp((JsonObject) response, groupId, dp, label, uid, type);
                    }
                }

            });

        } catch (Exception e) {
            logger.error("siemensHvac:ReadDp:Error during dp reading: " + dp + " ; " + e.getLocalizedMessage());
            // Reset sessionId so we redone _auth on error
        }
    }

}
