/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.linkyd2l.internal.handler;

import java.time.temporal.WeekFields;
import java.util.concurrent.ScheduledFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.linkyd2l.internal.LinkyD2LConfiguration;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link LinkyD2LHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Gaël L'hopital - Initial contribution
 */

@NonNullByDefault
public class LinkyD2LHandler extends BaseThingHandler {
    private static final int REFRESH_FIRST_HOUR_OF_DAY = 1;
    private static final int REFRESH_INTERVAL_IN_MIN = 120;

    private final Logger logger = LoggerFactory.getLogger(LinkyD2LHandler.class);

    private final HttpClient httpClient;
    private final Gson gson;
    private final WeekFields weekFields;

    private @Nullable ScheduledFuture<?> refreshJob;

    private @NonNullByDefault({}) String prmId;
    private @NonNullByDefault({}) String userId;

    public LinkyD2LHandler(Thing thing, LocaleProvider localeProvider, Gson gson, HttpClient httpClient) {
        super(thing);
        this.gson = gson;
        this.httpClient = httpClient;
        this.weekFields = WeekFields.of(localeProvider.getLocale());

    }

    @Override
    public void initialize() {
        logger.debug("Initializing Linky D2L handler.");
        updateStatus(ThingStatus.UNKNOWN);

        LinkyD2LConfiguration config = getConfigAs(LinkyD2LConfiguration.class);
        if (config.seemsValid()) {
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.config-error-mandatory-settings");
        }
    }

    /**
     * Request new data and updates channels
     */
    private synchronized void updateData() {
        boolean connectedBefore = isConnected();
    }

    private boolean isConnected() {
        return false;
    }

    private void disconnect() {
    }

    @Override
    public void dispose() {
        logger.debug("Disposing the Linky D2L handler.");
        ScheduledFuture<?> job = this.refreshJob;
        if (job != null && !job.isCancelled()) {
            job.cancel(true);
            refreshJob = null;
        }
        disconnect();
    }

    @Override
    public synchronized void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            logger.debug("Refreshing channel {}", channelUID.getId());
        } else {
            logger.debug("The Linky D2L binding is read-only and can not handle command {}", command);
        }
    }

}
