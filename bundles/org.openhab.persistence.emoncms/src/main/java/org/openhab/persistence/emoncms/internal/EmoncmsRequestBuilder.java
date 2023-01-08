/**
 * 
 */
package org.openhab.persistence.emoncms.internal;

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Nicolas
 *
 */
public class EmoncmsRequestBuilder {
	
	public static String FUNCTION_POST = "input/post.json?";
	public static String NODE = "node=";
	public static String JSON = "json=";
	public static final String API = "apikey=";
	private StringBuilder urlString;
	private String econcmsUrl;
	private String apiKey;
	private int node;

	private static final Logger logger = LoggerFactory
			.getLogger(EmoncmsRequestBuilder.class);




	public EmoncmsRequestBuilder(String econcmsUrl, String apiKey, int node){
		this.econcmsUrl = econcmsUrl;
		this.apiKey = apiKey;
		this.node = node;
		initRequest();
	}
	
	private void initRequest(){
		this.urlString = new StringBuilder(econcmsUrl);
		urlString.append(FUNCTION_POST)
		.append(NODE + node)
		.append("&" + API + apiKey)
		.append("&" + JSON + "{");
	}
	
	public void appendData(String key, String value){
		String data = key + ":" + value;
		if (urlString.length() + data.length() > 8000){
			finalizeRequest();
			}
		urlString.append(data)
		.append(",");		
	}
	
	public void finalizeRequest(){
		try {
			urlString.replace(urlString.lastIndexOf(","),
			urlString.lastIndexOf(",") + 1, "");
			urlString.append("}");
			URL url = new URL(urlString.toString());
			int size = urlString.length();
			initRequest();
			logger.debug("Posted  Size : " + size  +" . " + "Response from server  : " + url.getContent().toString());
		} catch (Exception e) {
			logger.error("Error posting request to emoncms : " + e);
		}
		
	}
	
}
