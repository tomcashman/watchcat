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
package com.viridiansoftware.watchcat.node.metrics.reporting;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Inserts the current metrics every second into ElasticSearch
 * 
 * @author Thomas Cashman
 */
@Component
public class ElasticSearchReporter implements Runnable {
	private static String WATCHCAT_INDEX = "watchcat";
	private static String WATCHCAT_TYPE = "host";

	@Autowired
	private LinuxMetricsCollector metricsCollector;
	@Autowired
	private ScheduledExecutorService scheduledExecutorService;
	@Autowired
	private TransportClient transportClient;
	@Autowired
    @Qualifier("hostname")
	private String hostname;

	@PostConstruct
	public void postConstruct() {
		try {
			registerHostInLinuxGraphIndex();
			setupIndexes();
		} catch (Exception e) {
			e.printStackTrace();
		}
		scheduledExecutorService.scheduleAtFixedRate(this, 5000, 1000,
				TimeUnit.MILLISECONDS);
	}

	@PreDestroy
	public void preDestroy() {
		if(transportClient.connectedNodes().size() > 0) {
			refreshIndex();
			transportClient.close();
		}
	}

	private void registerHostInLinuxGraphIndex() {
		try {
			XContentBuilder hostEntry = XContentFactory.jsonBuilder()
					.startObject().field("host", hostname).endObject();

			transportClient.prepareIndex(WATCHCAT_INDEX, WATCHCAT_TYPE, hostname)
					.setSource(hostEntry).execute().actionGet();
			transportClient.admin().indices().prepareRefresh(WATCHCAT_INDEX).execute()
					.actionGet();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		if(transportClient.connectedNodes().size() == 0) {
			System.exit(1);
		}
		
		BulkRequestBuilder bulkRequestBuilder = transportClient.prepareBulk();

		long timestamp = System.currentTimeMillis();
		String timestampStr = String.valueOf(timestamp);

		XContentBuilder loadAverage = metricsCollector.getLoadAverage().toJson(
				timestamp);
		if (loadAverage != null) {
			bulkRequestBuilder.add(transportClient.prepareIndex(hostname, "load",
					timestampStr).setSource(loadAverage));
		}

		XContentBuilder memoryUsage = metricsCollector.getMemoryUsage().toJson(
				timestamp);
		if (memoryUsage != null) {
			bulkRequestBuilder.add(transportClient.prepareIndex(hostname, "memory",
					timestampStr).setSource(memoryUsage));
		}

		XContentBuilder diskUsage = metricsCollector.getDiskUsage().toJson(
				timestamp);
		if (diskUsage != null) {
			bulkRequestBuilder.add(transportClient.prepareIndex(hostname, "disks",
					timestampStr).setSource(diskUsage));
		}

		XContentBuilder bandwidth = metricsCollector.getBandwidth().toJson(
				timestamp);
		if (bandwidth != null) {
			bulkRequestBuilder.add(transportClient.prepareIndex(hostname, "bandwidth",
					timestampStr).setSource(bandwidth));
		}

		XContentBuilder processes = metricsCollector.getProcesses().toJson(
				timestamp);
		if (processes != null) {
			bulkRequestBuilder.add(transportClient.prepareIndex(hostname, "processes",
					timestampStr).setSource(processes));
		}

		XContentBuilder networkConnections = metricsCollector
				.getNetworkConnections().toJson(timestamp);
		if (networkConnections != null) {
			bulkRequestBuilder.add(transportClient.prepareIndex(hostname, "connections",
					timestampStr).setSource(networkConnections));
		}

		if (bulkRequestBuilder.numberOfActions() > 0) {
			bulkRequestBuilder.execute().actionGet();
			refreshIndex();
		}
	}

	private void setupIndexes() throws Exception {
		IndicesAdminClient indicesAdminClient = transportClient.admin().indices();
		if (indicesAdminClient.exists(new IndicesExistsRequest(hostname))
				.actionGet().isExists()) {
			return;
		}

		if (!indicesAdminClient.prepareCreate(hostname).execute().actionGet()
				.isAcknowledged()) {
			// TODO: Throw exception
		}

		setupNestedConnectionType(indicesAdminClient);
	}
	
	private void setupNestedConnectionType(IndicesAdminClient indicesAdminClient) throws Exception {
		XContentBuilder nestedConnectionTypeBuilder = XContentFactory.jsonBuilder();
		nestedConnectionTypeBuilder.startObject();
		nestedConnectionTypeBuilder.startObject("connections");
		nestedConnectionTypeBuilder.startObject("properties");
		nestedConnectionTypeBuilder.startObject("connections");
		nestedConnectionTypeBuilder.field("type", "nested");
		nestedConnectionTypeBuilder.endObject();
		nestedConnectionTypeBuilder.endObject();
		nestedConnectionTypeBuilder.endObject();
		nestedConnectionTypeBuilder.endObject();

		if (!indicesAdminClient.preparePutMapping(hostname)
				.setType("connections").setSource(nestedConnectionTypeBuilder).execute()
				.actionGet().isAcknowledged()) {
			// TODO: Throw exception
		}
	}

	private void refreshIndex() {
		transportClient.admin().indices().prepareRefresh(hostname).execute().actionGet();
	}
}
