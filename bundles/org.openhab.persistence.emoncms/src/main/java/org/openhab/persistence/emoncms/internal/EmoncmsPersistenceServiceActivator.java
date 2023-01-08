/**
 * Copyright (c) 2015, Inria.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Nicolas Bonnefond - initial API and implementation and/or initial documentation
 */
package org.openhab.persistence.emoncms.internal;

import org.openhab.core.persistence.PersistenceService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the implementation of the Emoncms {@link PersistenceService}.
 * 
 * @author Nicolas Bonnefond (nicolas.bonnefond@inria.fr)
 */
public class EmoncmsPersistenceServiceActivator implements BundleActivator {

	private static final Logger logger = LoggerFactory
			.getLogger(EmoncmsPersistenceServiceActivator.class);

	/**
	 * Called whenever the OSGi framework starts our bundle
	 */
	public void start(BundleContext bc) throws Exception {
		logger.debug("Emoncms persistence bundle has been started.");
	}

	/**
	 * Called whenever the OSGi framework stops our bundle
	 */
	public void stop(BundleContext bc) throws Exception {
		logger.debug("Emoncms persistence bundle stopping.");
	}

}
