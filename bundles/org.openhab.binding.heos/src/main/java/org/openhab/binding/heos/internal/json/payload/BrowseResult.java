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
package org.openhab.binding.heos.internal.json.payload;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.annotations.SerializedName;

/**
 * Data class for response payloads from browse commands
 *
 * @author Martin van Wingerden - Initial contribution
 */
@NonNullByDefault
public class BrowseResult {
    private static final Logger logger = LoggerFactory.getLogger(BrowseResult.class);
    public @Nullable YesNoEnum container;
    @SerializedName("mid")
    public @Nullable String mediaId;
    public @Nullable YesNoEnum playable;
    public @Nullable BrowseResultType type;
    @SerializedName("cid")
    public @Nullable String containerId;
    public @Nullable String name;
    @SerializedName("image_url")
    private @Nullable String imageUrl;
    @SerializedName("sid")
    public @Nullable String sid;

    public boolean available;

    @Override
    public String toString() {
        return "BrowseResult{" + "container=" + container + ", mediaId='" + mediaId + '\'' + ", playable=" + playable
                + ", type=" + type + ", containerId='" + containerId + '\'' + ", sid='" + sid + '\'' + ", name='" + name
                + '\'' + ", imageUrl='" + imageUrl + '\'' + '}';
    }

    public String getImageUrl() {
        if (name.equals("TuneIn")) {
            return "/static/TuneIn.png";
        } else if (name.equals("Deezer")) {
            return "/static/Deezer.png";
        } else if (name.equals("Tidal")) {
            return "/static/Tidal.png";
        } else if (name.equals("Amazon")) {
            return "/static/Amazon.png";
        } else if (name.equals("SoundCloud")) {
            return "/static/SoundCloud.png";
        } else if (name.equals("Qobuz")) {
            return "/static/Qobuz.png";
        } else if (name.equals("Local Music")) {
            return "/static/LocalMusic.png";
        } else if (name.equals("Playlists")) {
            return "/static/playlist.png";
        } else if (name.equals("History")) {
            return "/static/History.png";
        } else if (name.equals("Favorites")) {
            return "/static/Favorites.png";
        } else if (name.equals("AUX Input")) {
            return "/static/AuxInput.png";
        } else {
            logger.info("name:" + name);
        }
        if (imageUrl != null) {
            return imageUrl;
        }
        return "";
    }
}
