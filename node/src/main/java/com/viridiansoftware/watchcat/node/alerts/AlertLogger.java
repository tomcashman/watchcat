/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Thomas Cashman
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
