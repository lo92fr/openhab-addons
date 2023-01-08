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
package org.openhab.binding.linkyd2l.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link LinkyD2LBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class LinkyD2LBindingConstants {

    public static final String BINDING_ID = "linkyd2l";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_LINKY_D2L = new ThingTypeUID(BINDING_ID, "linkyd2l");

    // Thing properties
    public static final String PUISSANCE = "puissance";
    public static final String PRM_ID = "prmId";
    public static final String USER_ID = "av2_interne_id";

    // List of all Channel id's
    public static final String YESTERDAY = "daily#yesterday";
    public static final String PEAK_POWER = "daily#power";
    public static final String PEAK_TIMESTAMP = "daily#timestamp";
    public static final String THIS_WEEK = "weekly#thisWeek";
    public static final String LAST_WEEK = "weekly#lastWeek";
    public static final String THIS_MONTH = "monthly#thisMonth";
    public static final String LAST_MONTH = "monthly#lastMonth";
    public static final String THIS_YEAR = "yearly#thisYear";
    public static final String LAST_YEAR = "yearly#lastYear";
}
