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
package com.viridiansoftware.custodian.collector.monitoring;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.viridiansoftware.custodian.collector.metrics.reporting.LinuxMetricsCollector;
import com.viridiansoftware.custodian.collector.monitoring.threshold.BandwidthThresholds;
import com.viridiansoftware.custodian.collector.monitoring.threshold.DiskUsageThresholds;
import com.viridiansoftware.custodian.collector.monitoring.threshold.LoadAverageThresholds;
import com.viridiansoftware.custodian.collector.monitoring.threshold.MemoryUsageThresholds;
import com.viridiansoftware.custodian.collector.monitoring.threshold.NetworkConnectionsThresholds;

/**
 * Monitors a {@link LinuxMetricsCollector} instance for alert threshold
 * breaches. When a breach occurs, an email is sent to the configured addresses.
 *
 * @author Thomas Cashman
 */
@Component
public class LinuxMetricsMonitor implements Runnable {
	@Autowired
	private LinuxMetricsCollector metricsCollector;
	@Autowired
	private ScheduledExecutorService scheduledExecutorService;

	@Autowired
	private BandwidthThresholds bandwidthThresholds;
	@Autowired
	private DiskUsageThresholds diskUsageThresholds;
	@Autowired
	private LoadAverageThresholds loadAverageThresholds;
	@Autowired
	private MemoryUsageThresholds memoryUsageThresholds;
	@Autowired
	private NetworkConnectionsThresholds networkConnectionsThresholds;

	@PostConstruct
	public void postConstruct() {
		scheduledExecutorService.scheduleAtFixedRate(this, 6000, 1000,
				TimeUnit.MILLISECONDS);
	}

	@Override
	public void run() {
		long timestamp = System.currentTimeMillis();
	}
}
