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

import java.util.Dictionary;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.openhab.core.items.Item;
import org.openhab.core.persistence.PersistenceService;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the implementation of the Emoncms {@link PersistenceService}.
 * 
 * @author Nicolas Bonnefond (nicolas.bonnefond@inria.fr)
 */
public class EmoncmsService implements PersistenceService {

	private static final Logger logger = LoggerFactory
			.getLogger(EmoncmsService.class);

	private String apiKey;
	private String url;
	private int node;
	private EmoncmsLogger emoncmsLogger = null;
	private boolean round;
	private int sendInterval;

	private final static String DEFAULT_EVENT_URL = "http://emoncms.org/";

	private final static int DEFAULT_NODE = 0;

	private final static boolean DEFAULT_ROUND = false;
	
	private final static int DEFAULT_SEND_INTERVAL = 0;

	private boolean initialized = false;

	/**
	 * @{inheritDoc
	 */
	public String getName() {
		return "emoncms";
	}

	public void deactivate() {
		this.emoncmsLogger = null;
	}

	/**
	 * @{inheritDoc
	 */
	public void store(Item item, String alias) {
		store(item);
	}
	

	/**
	 * @{inheritDoc
	 */
	public void store(Item item) {
		if (initialized) {
			this.emoncmsLogger.logEvent(item);
		} else {
			logger.error("emoncms persistence logger not initialized");
		}
	}

	  public void activate(final BundleContext bundleContext, final Map<String, Object> config) throws ConfigurationException {
		  logger.debug("emoncms persistence service activated");
		  this.initialized = false;

			this.emoncmsLogger = null;
			
			logger.debug("Parsing new configuration for emoncms persistence service");

			if (config != null) {

				this.url = (String) config.get("url");
				if (StringUtils.isBlank(url)) {
					this.url = DEFAULT_EVENT_URL;
				}

				try {
					this.node = Integer.parseInt((String) config.get("node"));
				} catch (Exception e) {
					logger.info("emoncms using default node : " + DEFAULT_NODE
							+ " error : " + e.getLocalizedMessage());
					this.node = DEFAULT_NODE;
				}
				
				try {
					this.sendInterval = Integer.parseInt((String) config.get("sendinterval"));
				} catch (Exception e) {
					logger.info("emoncms using default sendinterval : " + DEFAULT_SEND_INTERVAL
							+ " error : " + e.getLocalizedMessage());
					this.sendInterval = DEFAULT_SEND_INTERVAL;
				}

				try {
					this.round = (boolean) Boolean.parseBoolean((String) config
							.get("round"));
				} catch (Exception e) {
					logger.info("emoncms using default round : " + DEFAULT_ROUND
							+ " error : " + e.getLocalizedMessage());
					this.round = DEFAULT_ROUND;
				}

				this.apiKey = (String) config.get("apikey");
				if (StringUtils.isBlank(apiKey)) {
					throw new ConfigurationException("emoncms:apikey",
							"The emoncms API-Key is missing - please configure it in openhab.cfg");
				}
				
				this.initialized = true;

				this.emoncmsLogger = (this.sendInterval == 0 ? new EmoncmsLogger(url, apiKey, node, round) : new DelayedEmoncmsLogger(url, apiKey, node, round, sendInterval));
			}
	  }
	  
	/**
	 * @{inheritDoc
	 */
	@SuppressWarnings("rawtypes")
	public void updated(Dictionary config) throws ConfigurationException {
		//readConfig(config);
	}


}
