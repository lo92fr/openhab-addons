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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openmuc.jmbus.Bcd;
import org.openmuc.jmbus.DataRecord;
import org.openmuc.jmbus.VariableDataStructure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SiemensHvacHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Laurent ARNAL - Initial contribution
 */
@NonNullByDefault
public class MBusCounterHandlerImpl extends MBusBaseThingHandler implements MBusCounter {

    private final Logger logger = LoggerFactory.getLogger(MBusCounterHandlerImpl.class);

    private @Nullable ScheduledFuture<?> pollingJob = null;
    private boolean firstRead = true;
    private static Lock lck = new ReentrantLock();

    public MBusCounterHandlerImpl(Thing thing) {
        super(thing);

        logger.info("===========================================================");
        logger.info("MBus8");
        logger.info("===========================================================");
    }

    @Override
    public void initialize() {

        updateStatus(ThingStatus.UNKNOWN);

        logger.debug("initialize");
        scheduler.execute(() -> {
            boolean thingReachable = true;
            if (thingReachable) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
        });

        logger.debug("call test");
        setupChannel();

        pollingJob = scheduler.scheduleWithFixedDelay(this::pollingCode, 0, 10, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        if (pollingJob != null) {
            pollingJob.cancel(true);
        }
    }

    private void setupChannel() {

        try {
            lck.lock();
            logger.info("Search channel");
            int idx = 0;
            Map<String, String> properties = this.getThing().getProperties();
            String idS = properties.get("primaryAddr");
            if (idS != null) {
                idx = (int) Double.parseDouble(idS);
            }

            int retry = 0;
            VariableDataStructure result = null;

            while (retry < 3 && result == null) {
                logger.info("Reset Slave:" + idx);
                this.connector.resetSlave(idx);

                Thread.sleep(500);

                logger.info("Read Slave:" + idx);
                result = connector.readSlave(idx);
                retry++;
            }

            ThingBuilder thingBuilder = editThing();
            List<Channel> oldChannels = this.getThing().getChannels();
            List<Channel> newChannels = new ArrayList<Channel>();
            ChannelBuilder channelBuilder;

            List<DataRecord> dRecord = result.getDataRecords();
            int id = 0;
            for (DataRecord d : dRecord) {
                ChannelTypeUID cTypeUuid = new ChannelTypeUID("mbus", "power");

                channelBuilder = ChannelBuilder.create(new ChannelUID(this.getThing().getUID(), "mbus_" + id),
                        "String");
                channelBuilder.withLabel(d.getDescription().toString()).withDescription(d.getDescription().toString())
                        .withType(cTypeUuid);

                newChannels.add(channelBuilder.build());
                logger.info("Find record:" + d.getDescription() + " <> " + d.getDataValueType() + " <> "
                        + d.getDataValue());
                id++;
            }

            logger.info("update thing:" + idx);

            thingBuilder.withoutChannels(oldChannels).withChannels(newChannels);
            updateThing(thingBuilder.build());

            Thread.sleep(1000);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

        } finally {
            lck.unlock();
        }
    }

    private void pollingCode() {
        try {
            lck.lock();
            logger.info("poolingCode:Enter()");
            int idx = 0;
            Map<String, String> properties = this.getThing().getProperties();
            String idS = properties.get("primaryAddr");

            if (idS != null) {
                idx = (int) Double.parseDouble(idS);
            }

            // Make sure we don't overlap counter addressing on bus by adding delay on reseting address
            Thread.sleep(500);
            logger.info("Read counter:" + idx);
            this.connector.resetSlave(idx);
            Thread.sleep(500);
            VariableDataStructure result = connector.readSlave(idx);
            List<DataRecord> dRecord = result.getDataRecords();
            int id = 0;
            for (DataRecord d : dRecord) {
                logger.info("Find record:" + idx + " <> " + id + " <> " + d.getDescription() + " <> "
                        + d.getDataValueType() + " <> " + d.getDataValue());

                ChannelUID chanUid = new ChannelUID(this.getThing().getUID(), "mbus_" + id);
                int exp = d.getMultiplierExponent();
                Object obj = d.getDataValue();

                if (obj instanceof Long) {
                    double value = (long) obj;
                    if (exp != 0) {
                        double multiple = Math.pow(10, exp);
                        value = value * multiple;
                    }

                    updateState(chanUid, new DecimalType((float) value));
                } else if (obj instanceof Double) {
                    updateState(chanUid, new StringType("" + (double) obj));
                } else if (obj instanceof Bcd) {
                    Bcd bcd = (Bcd) obj;
                    long value = bcd.longValue();
                    updateState(chanUid, new StringType("" + value));
                } else if (obj instanceof String) {
                    String st = (String) obj;
                    updateState(chanUid, new StringType(st));
                } else {

                    logger.info("unknow type");
                }
                id++;
            }

            Thread.sleep(1000);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        } finally {
            lck.unlock();
        }
        logger.info("poolingCode:Exit()");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand");
        if (command instanceof RefreshType) {
            logger.debug("handleCommandRefresh");
        } else {

            Channel channel = getThing().getChannel(channelUID);
            if (channel == null) {
                return;
            }

        }
    }

    @Override
    protected String getDescription() {
        return "MBusCounter";
    }
}
