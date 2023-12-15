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
package org.openhab.binding.mbus.internal.network;

import java.util.Date;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.Request;
import org.openmuc.jmbus.MBusConnection;
import org.openmuc.jmbus.MBusConnection.MBusTcpBuilder;
import org.openmuc.jmbus.VariableDataStructure;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 *
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
@Component(immediate = true)
public class MBusConnectorImpl implements MBusConnector {

    private static final Logger logger = LoggerFactory.getLogger(MBusConnectorImpl.class);

    private @Nullable String host = "192.168.254.41";
    private int port = 10001;

    @SuppressWarnings("unused")
    private @Nullable Date lastUpdate;

    private @Nullable MBusConnection mBusConnection;

    @Activate
    public MBusConnectorImpl() {
        MBusTcpBuilder builder = MBusConnection.newTcpBuilder(host, port).setTimeout(1000).setConnectionTimeout(2000);
        try {
            mBusConnection = builder.build();
            logger.debug("mbus:: open Sap Connection");
        } catch (Exception ex) {
            logger.debug("exception: {}", ex.getMessage());
        }
    }

    @Override
    public void resetSlave(int idx) {
        try {
            // mBusConnection.linkReset(idx);
            Thread.sleep(500);
        } catch (Exception ex) {
            logger.info("exception: {}", ex.getMessage());
        }
    }

    @Override
    public @Nullable VariableDataStructure readSlave(int idx) {
        MBusConnection lcmBusConnection = mBusConnection;
        if (lcmBusConnection == null) {
            return null;
        }
        try {
            VariableDataStructure result = lcmBusConnection.read(idx);
            if (result != null) {
                return result;
            }
        } catch (Exception ex) {
            logger.info("exception: {}", ex.getMessage());
        }

        return null;
    }

    @Override
    public @Nullable JsonObject DoRequest(String req) {
        return null;
    }

    @Override
    public void WaitAllPendingRequest() {
    }

    @Override
    public void onComplete(@Nullable Request request) {
    }

    @Override
    public void onError(@Nullable Request request) {
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
