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

import java.util.HashMap;

import org.openhab.core.items.Item;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.persistence.PersistenceService;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the implementation of the Emoncms {@link PersistenceService}.
 * 
 * @author Nicolas Bonnefond (nicolas.bonnefond@inria.fr)
 */
public class EmoncmsLogger {

	protected static String apiKey;
	protected static String econcmsUrl;
	protected static int node;
	protected static boolean round;
	public static String FUNCTION_POST = "input/post.json?";
	public static String NODE = "node=";
	public static String JSON = "json=";
	public static final String API = "apikey=";
	public static final String UNINITIALIZED = "Uninitialized";
	public static EmoncmsRequestBuilder requestBuilder;

	private static final Logger logger = LoggerFactory
			.getLogger(EmoncmsLogger.class);

	public EmoncmsLogger(String econcmsUrl, String apiKey, int node,
			boolean round) {
		EmoncmsLogger.apiKey = apiKey;
		EmoncmsLogger.econcmsUrl = econcmsUrl;
		EmoncmsLogger.node = node;
		EmoncmsLogger.round = round;
		EmoncmsLogger.requestBuilder = new EmoncmsRequestBuilder(econcmsUrl, apiKey, node);
	}

	public void logEvent(Item item) {

		if (!(item.getState() instanceof UnDefType)) {
			HashMap<String, DecimalType> datas = new HashMap<String, DecimalType>();

			datas.put(item.getName(),  (DecimalType) item.getStateAs(DecimalType.class));

				postDatas(datas);

		} else {
			logger.debug("emoncms logger : object " + item.getName()
					+ " Uninitialized");
		}
	}

	protected static void postDatas(HashMap<String, DecimalType> datas) {
		
		
		synchronized (requestBuilder) {
			
		try {
			if (!datas.isEmpty() && datas!= null){
			logger.debug("trying to post " + datas.size() + " items");
			

				for (String key : datas.keySet()) {
					try {
						DecimalType state = datas.get(key);
						requestBuilder.appendData(key , "" + ( round ? state.intValue() : state.floatValue()));
					} catch (Exception e) {
						logger.error("Error handling item " + key + " with value " + datas.get(key).toString()+ " : " + e);
					}
				}
						requestBuilder.finalizeRequest();
					}
		} catch (Exception e) {
			logger.error("Error posting " + datas.size() + " datas to emoncms : " + e);
		}
	}
	}
}
