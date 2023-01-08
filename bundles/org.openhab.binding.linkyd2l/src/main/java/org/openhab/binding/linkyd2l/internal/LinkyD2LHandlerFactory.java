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

import static org.openhab.binding.linkyd2l.internal.LinkyD2LBindingConstants.THING_TYPE_LINKY_D2L;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.linkyd2l.internal.handler.LinkyD2LHandler;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;

/**
 * The {@link LinkyD2LHandlerFactory} is responsible for creating things handlers.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.linkyd2l")
public class LinkyD2LHandlerFactory extends BaseThingHandlerFactory {
    private static final DateTimeFormatter LINKY_D2L_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSSX");

    private final Logger logger = LoggerFactory.getLogger(LinkyD2LHandlerFactory.class);

    private final LocaleProvider localeProvider;
    private final Gson gson;
    private final HttpClient httpClient;

    @Activate
    public LinkyD2LHandlerFactory(final @Reference LocaleProvider localeProvider,
            final @Reference HttpClientFactory httpClientFactory) {
        this.localeProvider = localeProvider;
        this.gson = new GsonBuilder().registerTypeAdapter(ZonedDateTime.class,
                (JsonDeserializer<ZonedDateTime>) (json, type, jsonDeserializationContext) -> ZonedDateTime
                        .parse(json.getAsJsonPrimitive().getAsString(), LINKY_D2L_FORMATTER))
                .create();
        this.httpClient = httpClientFactory.createHttpClient(LinkyD2LBindingConstants.BINDING_ID);
    }

    @Override
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);
        httpClient.getSslContextFactory().setExcludeCipherSuites(new String[0]);
        httpClient.setFollowRedirects(false);
        try {
            httpClient.start();
        } catch (Exception e) {
            logger.warn("Unable to start Jetty HttpClient {}", e.getMessage());
        }
    }

    @Override
    protected void deactivate(ComponentContext componentContext) {
        super.deactivate(componentContext);
        try {
            httpClient.stop();
        } catch (Exception e) {
            logger.warn("Unable to stop Jetty HttpClient {}", e.getMessage());
        }
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return THING_TYPE_LINKY_D2L.equals(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        return supportsThingType(thingTypeUID) ? new LinkyD2LHandler(thing, localeProvider, gson, httpClient) : null;
    }
}
