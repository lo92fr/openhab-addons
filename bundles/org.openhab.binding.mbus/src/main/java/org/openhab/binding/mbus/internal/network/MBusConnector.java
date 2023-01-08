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
package org.openhab.binding.mbus.internal.network;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.Request;
import org.openhab.binding.mbus.internal.handler.MBusBaseThingHandler;
import org.openmuc.jmbus.VariableDataStructure;

import com.google.gson.JsonObject;

/**
 * @author Laurent Arnal - Initial contribution
 */
@NonNullByDefault
public interface MBusConnector {

    public @Nullable JsonObject DoRequest(String req);

    public void WaitAllPendingRequest();

    public void onComplete(Request request);

    public void onError(Request request);

    public void setMBusBridgeBaseThingHandler(@Nullable MBusBaseThingHandler hvacBridgeBaseThingHandler);

    public @Nullable VariableDataStructure readSlave(int idx);

    public void resetSlave(int idx);
}
