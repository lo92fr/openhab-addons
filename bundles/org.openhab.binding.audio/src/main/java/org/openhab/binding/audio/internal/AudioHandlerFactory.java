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

import static org.openhab.binding.audio.internal.AudioBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.audio.internal.config.AudioConfiguration;
import org.openhab.binding.audio.internal.handler.AudioHandler;
import org.openhab.core.audio.AudioManager;
import org.openhab.core.audio.JavaSoundAudioSink;
import org.openhab.core.audio.WebAudioAudioSink;
import org.openhab.core.media.MediaService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link UpnpControlHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Laurent Arnal - Initial contribution
 *
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.audio")
@NonNullByDefault
public class AudioHandlerFactory extends BaseThingHandlerFactory {
    final AudioConfiguration configuration = new AudioConfiguration();

    private final Logger logger = LoggerFactory.getLogger(AudioHandlerFactory.class);
    private final JavaSoundAudioSink javaSoundAudioSink;
    private final WebAudioAudioSink webAudioAudioSink;
    private final MediaService mediaService;
    private @NonNullByDefault({}) AudioManager audioManager;

    @Activate
    public AudioHandlerFactory(final @Reference JavaSoundAudioSink javaSoundAudioSink,
            final @Reference WebAudioAudioSink webAudioAudioSink, final @Reference MediaService mediaService,
            final @Reference AudioManager audioManager) {
        this.javaSoundAudioSink = javaSoundAudioSink;
        this.webAudioAudioSink = webAudioAudioSink;
        this.mediaService = mediaService;
        this.audioManager = audioManager;
        logger.info("================ init");
    }

    @Deactivate
    protected void deActivate() {
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_INTERNAL_AUDIO)) {
            return new AudioHandler(thing, javaSoundAudioSink, mediaService, audioManager);
        } else if (thingTypeUID.equals(THING_TYPE_WEB_AUDIO)) {
            return new AudioHandler(thing, webAudioAudioSink, mediaService, audioManager);
        }

        return null;
    }

}
