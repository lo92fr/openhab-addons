/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mbus.internal.old;

/**
 * This class is responsible for parsing the binding configuration.
 *
 * @author laurent@clae.net
 * @since 1.9.0-SNAPSHOT
 */
// public class mbusGenericBindingProvider extends AbstractGenericBindingProvider implements mbusBindingProvider {
// private static final Logger logger = LoggerFactory.getLogger(mbusGenericBindingProvider.class);
//
// /**
// * {@inheritDoc}
// */
// @Override
// public String getBindingType() {
// logger.debug("mbus:mbus getBindingType()!");
// return "mbus";
// }
//
// /**
// * @{inheritDoc
// */
// @Override
// public void validateItemType(Item item, String bindingConfig) throws BindingConfigParseException {
// // if (!(item instanceof SwitchItem || item instanceof DimmerItem)) {
// // throw new BindingConfigParseException("item '" + item.getName()
// // + "' is of type '" + item.getClass().getSimpleName()
// // +
// // "', only Switch- and DimmerItems are allowed - please check your *.items configuration");
// // }
// }
//
// /**
// * {@inheritDoc}
// */
// @Override
// public void processBindingConfiguration(String context, Item item, String bindingConfig)
// throws BindingConfigParseException {
// logger.debug("mbus:mbus processBindingConfiguration()!");
// super.processBindingConfiguration(context, item, bindingConfig);
//
// mbusBindingConfig config = new mbusBindingConfig(item.getName(), bindingConfig);
//
// // parse bindingconfig here ...
//
// addBindingConfig(item, config);
// }
//
// public Map<String, BindingConfig> getBindingConfigs() {
// return bindingConfigs;
// }
//
// /**
// * This is a helper class holding binding specific configuration details
// *
// * @author laurent@clae.net
// * @since 1.8.0-SNAPSHOT
// */
// class mbusBindingConfig implements BindingConfig {
// // put member fields here which holds the parsed values
// private String name;
// private String bindingConfig;
// private int mbusId;
// private int dib;
//
// public mbusBindingConfig(String name, String bindingConfig) {
// this.name = name;
// this.bindingConfig = bindingConfig;
//
// String[] bindingValues = bindingConfig.split(";");
//
// for (String bindingValue : bindingValues) {
// String[] bindingParts = bindingValue.split(":");
//
// String bindingKey = bindingParts[0];
// String bindingVal = bindingParts[1];
//
// if (bindingKey.equals("mbusId")) {
// this.mbusId = Integer.parseInt(bindingVal);
// } else if (bindingKey.equals("dib")) {
// this.dib = Integer.parseInt(bindingVal);
// }
// }
// }
//
// public int getmbusId() {
// return mbusId;
// }
//
// public String getName() {
// return name;
// }
//
// public int getDib() {
// return dib;
// }
//
// }
//
// }
