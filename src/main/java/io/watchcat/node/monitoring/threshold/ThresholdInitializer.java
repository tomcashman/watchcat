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
package io.watchcat.node.monitoring.threshold;

import io.watchcat.node.ElasticSearchConstants;

import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.transport.TransportClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Inserts default alert threshold values into ElasticSearch if none exist
 *
 * @author Thomas Cashman
 */
@Component
public class ThresholdInitializer {
	@Autowired
	private TransportClient transportClient;
	@Autowired
	@Qualifier("hostname")
	private String hostname;

	@Autowired
	private DiskThresholds diskThresholds;
	@Autowired
	private LoadAverageThresholds loadAverageThresholds;
	@Autowired
	private MemoryUsageThresholds memoryUsageThresholds;

	public void initializeThresholds() {
		checkHostnameIndex();
		checkLoadAverageThresholds();
		checkMemoryUsageThresholds();
		checkDiskUsageThresholds();
	}

	private void checkHostnameIndex() {
		IndicesAdminClient indicesAdminClient = transportClient.admin()
				.indices();
		if (indicesAdminClient.exists(new IndicesExistsRequest(hostname))
				.actionGet().isExists()) {
			return;
		}

		if (!indicesAdminClient.prepareCreate(hostname).execute().actionGet()
				.isAcknowledged()) {
			// TODO: Throw exception
		}
	}

	private void checkLoadAverageThresholds() {
		GetResponse response = transportClient
				.prepareGet(hostname, ElasticSearchConstants.THRESHOLD_TYPE,
						ElasticSearchConstants.LOAD_AVERAGE).execute()
				.actionGet();
		if (!response.isExists() || response.isSourceEmpty()) {
			IndexResponse indexResponse = transportClient
					.prepareIndex(hostname,
							ElasticSearchConstants.THRESHOLD_TYPE,
							ElasticSearchConstants.LOAD_AVERAGE)
					.setSource(loadAverageThresholds.toJson()).execute()
					.actionGet();
			
			if(!indexResponse.isCreated()) {
				//TODO: Alert unable to contact ElasticSearch
			}
		}
	}

	private void checkMemoryUsageThresholds() {
		GetResponse response = transportClient
				.prepareGet(hostname, ElasticSearchConstants.THRESHOLD_TYPE,
						ElasticSearchConstants.MEMORY_USAGE).execute()
				.actionGet();
		if (!response.isExists() || response.isSourceEmpty()) {
			IndexResponse indexResponse = transportClient
					.prepareIndex(hostname,
							ElasticSearchConstants.THRESHOLD_TYPE,
							ElasticSearchConstants.MEMORY_USAGE)
					.setSource(memoryUsageThresholds.toJson()).execute()
					.actionGet();
			
			if(!indexResponse.isCreated()) {
				//TODO: Alert unable to contact ElasticSearch
			}
		}
	}
	
	private void checkDiskUsageThresholds() {
		GetResponse response = transportClient
				.prepareGet(hostname, ElasticSearchConstants.THRESHOLD_TYPE,
						ElasticSearchConstants.DISKS).execute()
				.actionGet();
		if (!response.isExists() || response.isSourceEmpty()) {
			IndexResponse indexResponse = transportClient
					.prepareIndex(hostname,
							ElasticSearchConstants.THRESHOLD_TYPE,
							ElasticSearchConstants.DISKS)
					.setSource(diskThresholds.toJson()).execute()
					.actionGet();
			
			if(!indexResponse.isCreated()) {
				//TODO: Alert unable to contact ElasticSearch
			}
		}
	}
}
