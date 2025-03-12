/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.sedif.internal.constants;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link SedifBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Gaël L'hopital - Initial contribution
 * @author Laurent Arnal - Rewrite addon to use official dataconect API *
 */
@NonNullByDefault
public class SedifBindingConstants {

    public static final String BINDING_ID = "sedif";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SEDIF = new ThingTypeUID(BINDING_ID, "sedif");
    public static final ThingTypeUID THING_TYPE_WEB_SEDIF_BRIDGE = new ThingTypeUID(BINDING_ID, "sedif-web");

    public static final Set<ThingTypeUID> SUPPORTED_DEVICE_THING_TYPES_UIDS = Set.of(THING_TYPE_SEDIF,
            THING_TYPE_WEB_SEDIF_BRIDGE);

    public static final String SEDIF_BASE_GROUP = "sedif-base";

    public static final String CHANNEL_CONSUMPTION = "consumption";

}
