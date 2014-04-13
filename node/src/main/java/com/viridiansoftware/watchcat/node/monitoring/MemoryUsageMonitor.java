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
package com.viridiansoftware.watchcat.node.monitoring;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.viridiansoftware.watchcat.node.alerts.AlertSender;
import com.viridiansoftware.watchcat.node.event.Criticality;
import com.viridiansoftware.watchcat.node.event.memoryusage.RAMUsageEvent;
import com.viridiansoftware.watchcat.node.event.memoryusage.SwapUsageEvent;
import com.viridiansoftware.watchcat.node.metrics.MemoryUsage;
import com.viridiansoftware.watchcat.node.metrics.reporting.LinuxMetricsCollector;
import com.viridiansoftware.watchcat.node.monitoring.threshold.MemoryUsageThresholds;

/**
 * Monitors {@link MemoryUsage}
 *
 * @author Thomas Cashman
 */
@Component
public class MemoryUsageMonitor implements Runnable {
	@Autowired
	private LinuxMetricsCollector metricsCollector;
	@Autowired
	private ScheduledExecutorService scheduledExecutorService;
	@Autowired
	private AlertSender alertSender;
	@Autowired
	private MemoryUsageThresholds memoryUsageThresholds;
	
	private RAMUsageEvent ramUsageEvent;
	private SwapUsageEvent swapUsageEvent;
	
	@PostConstruct
	public void postConstruct() {
		scheduledExecutorService.schedule(this, 6000, TimeUnit.MILLISECONDS);
	}

	@Override
	public void run() {
		try {
			checkRAMUsage();
			checkSwapUsage();
		} catch (Exception e) {
			e.printStackTrace();
		}
		scheduledExecutorService.schedule(this, 5000, TimeUnit.MILLISECONDS);
	}

	private void checkRAMUsage() {
		MemoryUsage memoryUsage = metricsCollector.getMemoryUsage();
		
		double ramUsed = memoryUsage.getUsedMemory() / memoryUsage.getTotalMemory();
		if(ramUsed >= memoryUsageThresholds.getUsedMemoryCriticalThreshold()) {
			if(ramUsageEvent == null) {
				beginRamUsageEvent(Criticality.CRITICAL, ramUsed);
			} else {
				ramUsageEvent.updateStatus(Criticality.CRITICAL, String.valueOf(ramUsed));
			}
		} else if(ramUsed >= memoryUsageThresholds.getUsedMemoryMajorThreshold()) {
			if(ramUsageEvent == null) {
				beginRamUsageEvent(Criticality.MAJOR, ramUsed);
			} else {
				ramUsageEvent.updateStatus(Criticality.MAJOR, String.valueOf(ramUsed));
			}
		} else if(ramUsed >= memoryUsageThresholds.getUsedMemoryMinorThreshold()) {
			if(ramUsageEvent == null) {
				beginRamUsageEvent(Criticality.MINOR, ramUsed);
			} else {
				ramUsageEvent.updateStatus(Criticality.MINOR, String.valueOf(ramUsed));
			}
		} else if(ramUsageEvent != null) {
			ramUsageEvent.end();
			ramUsageEvent = null;
		}
	}
	
	private void beginRamUsageEvent(Criticality criticality, double ramUsed) {
		ramUsageEvent = new RAMUsageEvent(alertSender);
		ramUsageEvent.begin(criticality, String.valueOf(ramUsed));
	}
	
	private void checkSwapUsage() {
		MemoryUsage memoryUsage = metricsCollector.getMemoryUsage();
		
		double swapUsed = memoryUsage.getUsedSwap() / memoryUsage.getTotalSwap();
		if(swapUsed >= memoryUsageThresholds.getUsedSwapCriticalThreshold()) {
			if(swapUsageEvent == null) {
				beginSwapUsageEvent(Criticality.CRITICAL, swapUsed);
			} else {
				swapUsageEvent.updateStatus(Criticality.CRITICAL, String.valueOf(swapUsed));
			}
		} else if(swapUsed >= memoryUsageThresholds.getUsedSwapMajorThreshold()) {
			if(swapUsageEvent == null) {
				beginSwapUsageEvent(Criticality.MAJOR, swapUsed);
			} else {
				swapUsageEvent.updateStatus(Criticality.MAJOR, String.valueOf(swapUsed));
			}
		} else if(swapUsed >= memoryUsageThresholds.getUsedSwapMinorThreshold()) {
			if(swapUsageEvent == null) {
				beginSwapUsageEvent(Criticality.MINOR, swapUsed);
			} else {
				swapUsageEvent.updateStatus(Criticality.MINOR, String.valueOf(swapUsed));
			}
		} else if(swapUsageEvent != null) {
			swapUsageEvent.end();
			swapUsageEvent = null;
		}
	}
	
	private void beginSwapUsageEvent(Criticality criticality, double swapUsed) {
		swapUsageEvent = new SwapUsageEvent(alertSender);
		swapUsageEvent.begin(criticality, String.valueOf(swapUsed));
	}
}
