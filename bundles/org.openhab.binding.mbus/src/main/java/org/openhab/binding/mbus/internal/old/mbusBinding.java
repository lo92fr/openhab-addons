/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mbus.internal.old;

// import java.util.Map;
//
// import org.apache.commons.lang.StringUtils;
// import org.openhab.binding.mbus.mbusBindingProvider;
// import org.openhab.core.binding.AbstractActiveBinding;
// import org.openhab.core.events.EventPublisher;
// import org.openhab.core.types.Command;
// import org.openhab.core.types.State;
// import org.osgi.framework.BundleContext;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * Implement this class if you are going create an actively polling service like
 * querying a Website/Device.
 *
 * @author laurent@clae.net
 * @since 1.9.0-SNAPSHOT
 */

// public class mbusBinding extends AbstractActiveBinding<mbusBindingProvider> {
//
// private static final Logger logger = LoggerFactory.getLogger(mbusBinding.class);
//
// /**
// * The BundleContext. This is only valid when the bundle is ACTIVE. It is
// * set in the activate() method and must not be accessed anymore once the
// * deactivate() method was called or before activate() was called.
// */
// private BundleContext bundleContext;
// private mbusConnector connector;
//
// /**
// * the refresh interval which is used to poll values from the mbus
// * (optional, defaults to 60000ms)
// */
// private long refreshInterval = 60000;
//
// public mbusBinding() {
// logger.debug("mbus:mbusBinding() constructor is called!");
// }
//
// /**
// * Called by the SCR to activate the component with its configuration read
// * from CAS
// *
// * @param bundleContext
// * BundleContext of the Bundle that defines this component
// * @param configuration
// * Configuration properties for this component obtained from the
// * ConfigAdmin service
// */
// public void activate(final BundleContext bundleContext, final Map<String, Object> configuration) {
// logger.debug("mbus:activate() method is called!");
// this.bundleContext = bundleContext;
//
// connector = new mbusConnector(this);
//
// // to override the default refresh interval one has to add a
// // parameter to openhab.cfg like <bindingName>:refresh=<intervalInMs>
// String refreshIntervalString = (String) configuration.get("refresh");
// if (StringUtils.isNotBlank(refreshIntervalString)) {
// refreshInterval = Long.parseLong(refreshIntervalString);
// }
//
// String host = (String) configuration.get("host");
// if (StringUtils.isNotBlank(host)) {
// connector.setHost(host);
// }
//
// String port = (String) configuration.get("port");
// if (StringUtils.isNotBlank(port)) {
// connector.setPort(Integer.parseInt(port));
// }
//
// // read further config parameters here ...
//
// setProperlyConfigured(true);
// logger.debug("mbus:activate() method end!");
// }
//
// /**
// * Called by the SCR when the configuration of a binding has been changed
// * through the ConfigAdmin service.
// *
// * @param configuration
// * Updated configuration properties
// */
// public void modified(final Map<String, Object> configuration) {
// // update the internal configuration accordingly
// }
//
// /**
// * Called by the SCR to deactivate the component when either the
// * configuration is removed or mandatory references are no longer satisfied
// * or the component has simply been stopped.
// *
// * @param reason
// * Reason code for the deactivation:<br>
// * <ul>
// * <li>0 – Unspecified
// * <li>1 – The component was disabled
// * <li>2 – A reference became unsatisfied
// * <li>3 – A configuration was changed
// * <li>4 – A configuration was deleted
// * <li>5 – The component was disposed
// * <li>6 – The bundle was stopped
// * </ul>
// */
// public void deactivate(final int reason) {
// this.bundleContext = null;
// // deallocate resources here that are no longer needed and
// // should be reset when activating this binding again
// }
//
// /**
// * @{inheritDoc
// */
// @Override
// protected long getRefreshInterval() {
// return refreshInterval;
// }
//
// /**
// * @{inheritDoc
// */
// @Override
// protected String getName() {
// return "mbus Refresh Service";
// }
//
// /**
// * @{inheritDoc
// */
// @Override
// protected void execute() {
// // the frequently executed code (polling) goes here ...
// logger.debug("mbus:execute() method is called!");
//
// connector.ReadAllDp();
//
// logger.debug("mbus:execute() method return !");
// return;
// }
//
// /**
// * @{inheritDoc
// */
// @Override
// protected void internalReceiveCommand(String itemName, Command command) {
// // the code being executed when a command was sent on the openHAB
// // event bus goes here. This method is only called if one of the
// // BindingProviders provide a binding for the given 'itemName'.
// logger.debug("mbus:internalReceiveCommand({},{}) is called!", itemName, command);
// connector.AddDpUpdate(itemName, command);
// }
//
// /**
// * @{inheritDoc
// */
// @Override
// protected void internalReceiveUpdate(String itemName, State newState) {
// // the code being executed when a state was sent on the openHAB
// // event bus goes here. This method is only called if one of the
// // BindingProviders provide a binding for the given 'itemName'.
// }
//
// public mbusGenericBindingProvider getProvider() {
// return (mbusGenericBindingProvider) providers.toArray()[0];
// }
//
// public BundleContext getBundleContext() {
// return bundleContext;
// }
//
// public EventPublisher getEventPublisher() {
// return eventPublisher;
// }
//
// }
