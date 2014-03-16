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
package com.linuxgraph.collector.metrics.reporting;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.linuxgraph.collector.metrics.Bandwidth;
import com.linuxgraph.collector.metrics.DiskUsage;
import com.linuxgraph.collector.metrics.LoadAverage;
import com.linuxgraph.collector.metrics.MemoryUsage;
import com.linuxgraph.collector.metrics.NetworkConnections;
import com.linuxgraph.collector.metrics.Processes;

/**
 * Gathers metrics of the Linux system every second
 *
 * @author Thomas Cashman
 */
@Component
public class LinuxMetricsCollector {
	@Autowired
	private ScheduledExecutorService scheduledExecutorService;
	
	@Autowired
	private MemoryUsage memoryUsage;
	@Autowired
	private LoadAverage loadAverage;
	@Autowired
	private DiskUsage diskUsage;
	@Autowired
	private Bandwidth bandwidth;
	@Autowired
	private Processes processes;
	@Autowired
	private NetworkConnections networkConnections;
	
	@PostConstruct
	public void postConstruct() {
		scheduledExecutorService.scheduleAtFixedRate(loadAverage, 1000, 1000, TimeUnit.MILLISECONDS);
		scheduledExecutorService.scheduleAtFixedRate(memoryUsage, 1000, 1000, TimeUnit.MILLISECONDS);
		scheduledExecutorService.scheduleAtFixedRate(diskUsage, 1000, 1000, TimeUnit.MILLISECONDS);
		scheduledExecutorService.scheduleAtFixedRate(bandwidth, 1000, 1000, TimeUnit.MILLISECONDS);
		scheduledExecutorService.scheduleAtFixedRate(processes, 1000, 1000, TimeUnit.MILLISECONDS);
		scheduledExecutorService.scheduleAtFixedRate(networkConnections, 1000, 1000, TimeUnit.MILLISECONDS);
	}

	public MemoryUsage getMemoryUsage() {
		return memoryUsage;
	}

	public LoadAverage getLoadAverage() {
		return loadAverage;
	}

	public DiskUsage getDiskUsage() {
		return diskUsage;
	}

	public Bandwidth getBandwidth() {
		return bandwidth;
	}

	public Processes getProcesses() {
		return processes;
	}

	public NetworkConnections getNetworkConnections() {
		return networkConnections;
	}
}
