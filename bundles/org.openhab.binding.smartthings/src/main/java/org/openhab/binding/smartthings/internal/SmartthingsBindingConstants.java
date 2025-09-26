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
package org.openhab.binding.smartthings.internal;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link SmartthingsBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Bob Raker - Initial contribution
 */
@NonNullByDefault
public class SmartthingsBindingConstants {

    public static final String BINDING_ID = "smartthings";

    // List of Bridge Type UIDs
    public static final ThingTypeUID THING_TYPE_SMARTTHINGS = new ThingTypeUID(BINDING_ID, "smartthings");

    public static final ThingTypeUID THING_TYPE_SMARTTHINGSCLOUD = new ThingTypeUID(BINDING_ID, "smartthingscloud");

    // Authorization related Servlet and resources aliases.
    public static final String SMARTTHINGS_ALIAS = "/connectsmartthings";
    public static final String SMARTTHINGS_IMG_ALIAS = "/img";

    /**
     * Smartthings scopes needed by this binding to work.
     */
    public static final String SMARTTHINGS_SCOPES = Stream.of("r:devices:*", "w:devices:*", "x:devices:*", "r:hubs:*",
            "r:locations:*", "w:locations:*", "x:locations:*", "r:scenes:*", "x:scenes:*", "r:rules:*", "w:rules:*",
            "r:installedapps", "w:installedapps").collect(Collectors.joining(" "));

    // List of Spotify services related urls, information
    public static final String SMARTTHINGS_ACCOUNT_URL = "https://api.smartthings.com/oauth";
    public static final String SMARTTHINGS_AUTHORIZE_URL = SMARTTHINGS_ACCOUNT_URL + "/authorize";
    public static final String SMARTTHINGS_API_TOKEN_URL = SMARTTHINGS_ACCOUNT_URL + "/token";

    // List of all Thing Type UIDs
    // I tried to replace this with a dynamic processing of the thing-types.xml file using the ThingTypeRegistry
    // But the HandlerFactory wants to start checking on things before that code runs. So, back to a hard coded list

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(SmartthingsBindingConstants.THING_TYPE_SMARTTHINGS,
                    SmartthingsBindingConstants.THING_TYPE_SMARTTHINGSCLOUD).collect(Collectors.toSet()));

    public static final String THING_LIGHT = "light";

    public static final String CATEGORY_THING_SMARTTHINGS = "Smartthings";

    public static final String CONFIG_DESCRIPTION_URI_THING_PREFIX = "thing-type";

    // Event Handler Topics
    public static final String STATE_EVENT_TOPIC = "org/openhab/binding/smartthings/state";
    public static final String DISCOVERY_EVENT_TOPIC = "org/openhab/binding/smartthings/discovery";

    // Bridge config properties
    public static final String IP_ADDRESS = "ipAddress";
    public static final String PORT = "port";

    // Thing config properties
    public static final String SMARTTHINGS_NAME = "smartthingsName";
    public static final String THING_TIMEOUT = "timeout";
}
