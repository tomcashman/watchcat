/**
 * Copyright 2014 Thomas Cashman
 */
package com.viridiansoftware.watchcat.node.alerts;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import com.viridiansoftware.watchcat.node.ElasticSearchConstants;
import com.viridiansoftware.watchcat.node.event.Criticality;

/**
 *
 *
 * @author Thomas Cashman
 */
public class AlertLogger {
	private Criticality criticality;
	private String message;
	private String hostname;
	
	public AlertLogger(String hostname, Criticality criticality, String message) {
		this.criticality = criticality;
		this.message = message;
		this.hostname = hostname;
	}

	public void log(TransportClient transportClient) {
		long timestamp = System.currentTimeMillis();
		
		String id = hostname + "-" + timestamp;
		
		try {
			XContentBuilder builder = XContentFactory.jsonBuilder();
			builder = builder.startObject();
			builder = builder.field("hostname", hostname);
			builder = builder.field("timestamp", timestamp);
			builder = builder.field("criticality", criticality);
			builder = builder.field("message", message);
			builder = builder.endObject();
			
			transportClient.prepareIndex(ElasticSearchConstants.WATCHCAT_ALERTS_INDEX,
					ElasticSearchConstants.LOAD_AVERAGE, id).setSource(builder).execute().actionGet();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
