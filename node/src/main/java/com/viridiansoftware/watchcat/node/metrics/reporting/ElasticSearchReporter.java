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

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.ImmutableSettings.Builder;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.viridiansoftware.watchcat.node.util.ShellCommand;

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
	@Value("${cluster.name}")
	private String clusterName;
	@Value("${cluster.nodes}")
	private String clusterNodes;
	@Autowired
	private ScheduledExecutorService scheduledExecutorService;

	private String hostname;
	private TransportClient transportClient;

	public ElasticSearchReporter() {
		ShellCommand getHostname = new ShellCommand("cat /etc/hostname");
		hostname = getHostname.execute().replace("\n", "");

		if (hostname == null || hostname.length() == 0) {
			try {
				hostname = InetAddress.getLocalHost().getHostName();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@PostConstruct
	public void postConstruct() {
		try {
			buildElasticSearchClient();
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

	private void buildElasticSearchClient() throws IOException {
		Builder settingsBuilder = ImmutableSettings.settingsBuilder();

		settingsBuilder = settingsBuilder.put("cluster.name", clusterName);
		Settings settings = settingsBuilder.build();

		transportClient = new TransportClient(settings);
		if (clusterNodes != null && clusterNodes.length() > 0) {
			String[] nodes = clusterNodes.split(",");
			for (int i = 0; i < nodes.length; i++) {
				String[] address = nodes[i].split(":");
				if (address.length < 2) {
					transportClient = transportClient
							.addTransportAddress(new InetSocketTransportAddress(
									address[0], 9300));
				} else {
					transportClient = transportClient
							.addTransportAddress(new InetSocketTransportAddress(
									address[0], Integer.parseInt(address[1])));
				}
			}
		} else {
			transportClient = transportClient
					.addTransportAddress(new InetSocketTransportAddress(
							"localhost", 9300));
		}
		try {
			Thread.sleep(1000);
		} catch (Exception e) {}
		if(transportClient.connectedNodes().size() == 0) {
			throw new IOException("No nodes available");
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
