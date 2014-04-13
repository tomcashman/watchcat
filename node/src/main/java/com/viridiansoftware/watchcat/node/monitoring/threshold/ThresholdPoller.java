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
package com.viridiansoftware.watchcat.node.monitoring.threshold;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.viridiansoftware.watchcat.node.ElasticSearchConstants;

/**
 * Polls the alert threshold settings from ElasticSearch
 *
 * @author Thomas Cashman
 */
@Component
public class ThresholdPoller implements Runnable {
	@Autowired
	private TransportClient transportClient;
	@Autowired
	private ScheduledExecutorService scheduledExecutorService;
	@Autowired
	@Qualifier("hostname")
	private String hostname;

	@Autowired
	private FilesystemThresholds diskUsageThresholds;
	@Autowired
	private LoadAverageThresholds loadAverageThresholds;
	@Autowired
	private MemoryUsageThresholds memoryUsageThresholds;

	@Autowired
	private ThresholdInitializer thresholdInitializer;

	@PostConstruct
	public void postConstruct() {
		thresholdInitializer.initializeThresholds();
		scheduledExecutorService.scheduleAtFixedRate(this, 5000, 10000,
				TimeUnit.MILLISECONDS);
	}

	@Override
	public void run() {
		pollLoadAverageThresholds();
		pollMemoryUsageThresholds();
		pollDiskUsageThresholds();
	}

	private void pollLoadAverageThresholds() {
		GetResponse getResponse = transportClient
				.prepareGet(hostname, ElasticSearchConstants.THRESHOLD_TYPE,
						ElasticSearchConstants.LOAD_AVERAGE).execute()
				.actionGet();
		loadAverageThresholds.fromJson(getResponse);
	}
	
	private void pollMemoryUsageThresholds() {
		GetResponse getResponse = transportClient
				.prepareGet(hostname, ElasticSearchConstants.THRESHOLD_TYPE,
						ElasticSearchConstants.MEMORY_USAGE).execute()
				.actionGet();
		memoryUsageThresholds.fromJson(getResponse);
	}
	
	private void pollDiskUsageThresholds() {
		GetResponse getResponse = transportClient
				.prepareGet(hostname, ElasticSearchConstants.THRESHOLD_TYPE,
						ElasticSearchConstants.FILESYSTEMS).execute()
				.actionGet();
		diskUsageThresholds.fromJson(getResponse);
	}
}
