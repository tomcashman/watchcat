/**
 * Copyright 2014 Thomas Cashman
 */
package com.viridiansoftware.watchcat.node;

import java.io.IOException;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.ImmutableSettings.Builder;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Builds the {@link TransportClient} bean for autowiring
 *
 * @author Thomas Cashman
 */
@Configuration
public class TransportClientBuilder {
	@Value("${cluster.name}")
	private String clusterName;
	@Value("${cluster.nodes}")
	private String clusterNodes;
	
	@Bean
	public TransportClient getTransportClient() throws IOException {
		Builder settingsBuilder = ImmutableSettings.settingsBuilder();

		settingsBuilder = settingsBuilder.put("cluster.name", clusterName);
		Settings settings = settingsBuilder.build();

		TransportClient transportClient = new TransportClient(settings);
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
		return transportClient;
	}
}
