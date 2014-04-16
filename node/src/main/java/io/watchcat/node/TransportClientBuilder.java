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
package io.watchcat.node;

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
