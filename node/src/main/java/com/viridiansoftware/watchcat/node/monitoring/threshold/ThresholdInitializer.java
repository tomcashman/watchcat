/**
 * Copyright 2014 Thomas Cashman
 */
package com.viridiansoftware.watchcat.node.monitoring.threshold;

import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.transport.TransportClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.viridiansoftware.watchcat.node.ElasticSearchConstants;

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
	private BandwidthThresholds bandwidthThresholds;
	@Autowired
	private FilesystemThresholds diskUsageThresholds;
	@Autowired
	private LoadAverageThresholds loadAverageThresholds;
	@Autowired
	private MemoryUsageThresholds memoryUsageThresholds;
	@Autowired
	private NetworkConnectionsThresholds networkConnectionsThresholds;

	public void initializeThresholds() {
		checkHostnameIndex();
		checkLoadAverageThresholds();
		checkMemoryUsageThresholds();
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

	}
}
