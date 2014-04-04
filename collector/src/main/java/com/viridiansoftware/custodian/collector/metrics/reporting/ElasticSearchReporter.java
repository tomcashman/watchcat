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
package com.viridiansoftware.custodian.collector.metrics.reporting;

import java.net.InetAddress;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.client.Client;
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

import com.viridiansoftware.custodian.collector.util.ShellCommand;

/**
 * Inserts the current metrics every second into ElasticSearch
 * 
 * @author Thomas Cashman
 */
@Component
public class ElasticSearchReporter implements Runnable {
	private static String CUSTODIAN_INDEX = "custodian";
	private static String CUSTODIAN_TYPE = "host";

	@Autowired
	private LinuxMetricsCollector metricsCollector;
	@Value("${cluster.name}")
	private String clusterName;
	@Value("${cluster.nodes}")
	private String clusterNodes;
	@Autowired
	private ScheduledExecutorService scheduledExecutorService;

	private String hostname;
	private Client client;

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
		buildElasticSearchClient();
		registerHostInLinuxGraphIndex();

		scheduledExecutorService.scheduleAtFixedRate(this, 5000, 1000,
				TimeUnit.MILLISECONDS);
	}

	@PreDestroy
	public void preDestroy() {
		refreshIndex();
		client.close();

		scheduledExecutorService.shutdown();
	}

	private void buildElasticSearchClient() {
		Builder settingsBuilder = ImmutableSettings.settingsBuilder();

		settingsBuilder = settingsBuilder.put("cluster.name", clusterName);
		Settings settings = settingsBuilder.build();
		
		TransportClient transportClient = new TransportClient(settings);
		if(clusterNodes != null && clusterNodes.length() > 0) {
			String [] nodes = clusterNodes.split(",");
			for(int i = 0; i < nodes.length; i++) {
				String [] address = nodes[i].split(":");
				if(address.length < 2) {
					transportClient = transportClient.addTransportAddress(new InetSocketTransportAddress(
							address[0], 9300));
				} else {
					transportClient = transportClient.addTransportAddress(new InetSocketTransportAddress(
							address[0], Integer.parseInt(address[1])));
				}
			}
		} else {
			transportClient = transportClient.addTransportAddress(new InetSocketTransportAddress(
						"localhost", 9300));
		}

		client = transportClient;
	}

	private void registerHostInLinuxGraphIndex() {
		try {
			XContentBuilder hostEntry = XContentFactory.jsonBuilder().startObject().field("host", hostname).endObject();
			
			client.prepareIndex(CUSTODIAN_INDEX, CUSTODIAN_TYPE, hostname)
					.setSource(hostEntry).execute()
					.actionGet();
			client.admin().indices().prepareRefresh(CUSTODIAN_INDEX).execute()
					.actionGet();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	@Override
	public void run() {
		BulkRequestBuilder bulkRequestBuilder = client.prepareBulk();

		long timestamp = System.currentTimeMillis();
		String timestampStr = String.valueOf(timestamp);

		XContentBuilder loadAverage = metricsCollector.getLoadAverage().toJson(
				timestamp);
		if (loadAverage != null) {
			bulkRequestBuilder.add(client.prepareIndex(hostname, "load", timestampStr)
					.setSource(loadAverage));
		}

		XContentBuilder memoryUsage = metricsCollector.getMemoryUsage().toJson(
				timestamp);
		if (memoryUsage != null) {
			bulkRequestBuilder.add(client.prepareIndex(hostname, "memory", timestampStr)
					.setSource(memoryUsage));
		}

		XContentBuilder diskUsage = metricsCollector.getDiskUsage().toJson(
				timestamp);
		if (diskUsage != null) {
			bulkRequestBuilder.add(client.prepareIndex(hostname, "disks", timestampStr)
					.setSource(diskUsage));
		}
		
		XContentBuilder bandwidth = metricsCollector.getBandwidth().toJson(timestamp);
		if (bandwidth != null) {
			bulkRequestBuilder.add(client.prepareIndex(hostname, "bandwidth", timestampStr)
					.setSource(bandwidth));
		}
		
		XContentBuilder processes = metricsCollector.getProcesses().toJson(timestamp);
		if(processes != null) {
			bulkRequestBuilder.add(client.prepareIndex(hostname, "processes", timestampStr)
					.setSource(processes));
		}
		
		XContentBuilder networkConnections = metricsCollector.getNetworkConnections().toJson(timestamp);
		if(networkConnections != null) {
			bulkRequestBuilder.add(client.prepareIndex(hostname, "connections", timestampStr)
					.setSource(networkConnections));
		}

		if (bulkRequestBuilder.numberOfActions() > 0) {
			bulkRequestBuilder.execute().actionGet();
			refreshIndex();
		}
	}

	private void refreshIndex() {
		client.admin().indices().prepareRefresh(hostname).execute().actionGet();
	}
}
