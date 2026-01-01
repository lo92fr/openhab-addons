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
package org.openhab.binding.audio.internal.handler;

import static org.openhab.binding.audio.internal.AudioBindingConstants.*;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.audio.internal.AudioBindingConstants;
import org.openhab.core.audio.AudioManager;
import org.openhab.core.audio.AudioSink;
import org.openhab.core.audio.JavaSoundAudioSink;
import org.openhab.core.audio.URLAudioStream;
import org.openhab.core.audio.WebAudioAudioSink;
import org.openhab.core.library.types.MediaCommandEnumType;
import org.openhab.core.library.types.MediaCommandType;
import org.openhab.core.library.types.MediaStateType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.media.MediaListenner;
import org.openhab.core.media.MediaService;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link UpnpHandler} is the base class for {@link UpnpRendererHandler} and {@link UpnpServerHandler}. The base
 * class implements UPnPConnectionManager service actions.
 *
 * @author Laurent Arnal - Initial contribution
 *
 */
@NonNullByDefault
public class AudioHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(AudioHandler.class);
    private final AudioSink audioSink;
    private @Nullable JavaSoundAudioSink javaAudioSink;
    private @Nullable WebAudioAudioSink webAudioSink;
    private final MediaService mediaService;
    private @NonNullByDefault({}) AudioManager audioManager;

    private String artistName = "";
    private String albumName = "";
    private String trackName = "";

    public AudioHandler(Thing thing, AudioSink audioSink, MediaService mediaService, AudioManager audioManager) {
        super(thing);

        this.audioSink = audioSink;
        if (audioSink instanceof JavaSoundAudioSink) {
            javaAudioSink = (JavaSoundAudioSink) audioSink;
        } else if (audioSink instanceof WebAudioAudioSink) {
            webAudioSink = (WebAudioAudioSink) audioSink;
        }

        this.mediaService = mediaService;
        this.audioManager = audioManager;

    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void dispose() {
    }

    private void updateControlState() {
        PlayPauseType state = PlayPauseType.NONE;
        if ("PLAYING".equals("PLAYING")) {
            state = PlayPauseType.PLAY;
        } else if ("STOPPED".equals("")) {
            state = PlayPauseType.PAUSE;
        } else if ("PAUSED_PLAYBACK".equals("")) {
            state = PlayPauseType.PAUSE;
        }

        double trackPosition = 0;
        double trackDuration = 0;

        MediaStateType mediaStateType = new MediaStateType(state, new StringType(this.getThing().getUID().getId()),
                new StringType(AudioBindingConstants.BINDING_ID));

        mediaStateType.setCurrentPlayingPosition(trackPosition * 1000.00);
        mediaStateType.setCurrentPlayingTrackDuration(trackDuration * 1000.00);
        mediaStateType.setCurrentPlayingArtistName(artistName);
        // mediaStateType.set(artistName);
        mediaStateType.setCurrentPlayingTrackName(trackName);
        // mediaStateType.setCurrentPlayingArtUri(artUri);
        // mediaStateType.setCurrentPlayingVolume(getCurrentVolume().doubleValue());
        updateState(CONTROL, mediaStateType);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // TODO Auto-generated method stub

        String id = channelUID.getId();

        if (id.endsWith("volume")) {
            if (javaAudioSink != null) {
                try {
                    if (command instanceof RefreshType) {
                        updateState(VOLUME, javaAudioSink.getVolume());

                    } else if (command instanceof PercentType percentCommand) {
                        javaAudioSink.setVolume(percentCommand);
                    }
                } catch (IOException ex) {

                }
            }
        }

        if (command instanceof MediaCommandType mediaType) {
            MediaCommandEnumType commandType = mediaType.getCommand();
            StringType param = mediaType.getParam();
            String val = mediaType.getParam().toFullString();

            if (commandType == MediaCommandEnumType.PLAY) {
                MediaListenner mediaListenner = mediaService.getMediaListenner("6DB0E5AF-63C3-4901-B792-504CE4935B05");
                String streamUri = mediaListenner.getStreamUri(val);
                logger.info("Stream uri is:{}", streamUri);

                albumName = "album";
                artistName = "artist";
                trackName = val;
                updateState(TITLE, new StringType(trackName));
                updateState(ALBUM, new StringType(albumName));
                updateState(ARTIST, new StringType(artistName));
                updateControlState();

                if (!streamUri.isBlank()) {
                    try {
                        URLAudioStream urlAudioStream = new URLAudioStream(streamUri);
                        audioManager.play(urlAudioStream, this.audioSink.getId());

                    } catch (Exception ex) {

                        logger.info("");
                    }

                }

            }

            logger.info("");

        }

    }
}
