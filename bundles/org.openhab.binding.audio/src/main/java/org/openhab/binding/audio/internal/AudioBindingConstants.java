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
package org.openhab.binding.audio.internal;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link UpnpControlBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Laurent Arnal - Initial contribution
 *
 */
@NonNullByDefault
public class AudioBindingConstants {

    public static final String BINDING_ID = "audio";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_INTERNAL_AUDIO = new ThingTypeUID(BINDING_ID, "internalaudio");
    public static final ThingTypeUID THING_TYPE_WEB_AUDIO = new ThingTypeUID(BINDING_ID, "webaudio");
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .of(THING_TYPE_INTERNAL_AUDIO, THING_TYPE_WEB_AUDIO).collect(Collectors.toSet());

    // List of all Channel ids
    public static final String VOLUME = "volume";
    public static final String MUTE = "mute";
    public static final String CONTROL = "control";
    public static final String STOP = "stop";
    public static final String URI = "uri";
    public static final String TITLE = "title";
    public static final String ALBUM = "album";
    public static final String ALBUM_ART = "albumart";
    public static final String ARTIST = "artist";
}
