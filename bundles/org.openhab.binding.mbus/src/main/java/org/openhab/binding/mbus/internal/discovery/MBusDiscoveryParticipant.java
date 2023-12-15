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
package org.openhab.binding.mbus.internal.discovery;

/**
 * The {@link MBusDiscoveryParticipant} is responsible for discovering new and
 * removed mbus bridges. It uses the central {@link UpnpDiscoveryService}.
 *
 * @author Laurent Arnal - Initial contribution
 */

// @NonNullByDefault
// @Component(service = UpnpDiscoveryParticipant.class)
// public class MBusDiscoveryParticipant implements UpnpDiscoveryParticipant {
//
// @Override
// public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
// return Collections.singleton(MBusBindingConstants.THING_TYPE_MBUSTCPGATEWAY);
// }
//
// @Override
// public @Nullable DiscoveryResult createResult(RemoteDevice device) {
// // ThingUID uid = getThingUID(device);
// // if (uid != null) {
// // Map<String, Object> properties = new HashMap<>();
// // String ipAddress = device.getDetails().getPresentationURI().getHost();
// // properties.put(SiemensHvacBindingConstants.IP_ADDRESS, ipAddress);
// // properties.put(SiemensHvacBindingConstants.BASE_URL, "https://" + ipAddress + "/");
// //
// // String serialNumber = device.getDetails().getSerialNumber();
// // DiscoveryResult result;
// // if (serialNumber != null && !serialNumber.isBlank()) {
// // properties.put(PROPERTY_SERIAL_NUMBER, serialNumber.toLowerCase());
// //
// // result = DiscoveryResultBuilder.create(uid).withProperties(properties)
// // .withLabel(device.getDetails().getFriendlyName())
// // .withRepresentationProperty(PROPERTY_SERIAL_NUMBER).build();
// // } else {
// // result = DiscoveryResultBuilder.create(uid).withProperties(properties)
// // .withLabel(device.getDetails().getFriendlyName()).build();
// // }
// // return result;
// // } else {
// // return null;
// // }
// return null;
// }
//
// @Override
// public @Nullable ThingUID getThingUID(RemoteDevice device) {
// // DeviceDetails details = device.getDetails();
// // if (details != null) {
// // ModelDetails modelDetails = details.getModelDetails();
// // String serialNumber = details.getSerialNumber();
// // if (modelDetails != null && serialNumber != null && !serialNumber.isBlank()) {
// // String modelName = modelDetails.getModelName();
// // if (modelName != null) {
// // if (modelName.startsWith("Web Server OZW672.01")) {
// // return new ThingUID(SiemensHvacBindingConstants.THING_TYPE_OZW672, serialNumber.toLowerCase());
// // }
// // }
// // }
// // }
// return null;
// }
// }
