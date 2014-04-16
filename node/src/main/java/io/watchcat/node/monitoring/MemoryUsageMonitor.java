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
package io.watchcat.node.monitoring;

import io.watchcat.node.alerts.AlertSender;
import io.watchcat.node.event.Criticality;
import io.watchcat.node.event.memoryusage.RAMUsageEvent;
import io.watchcat.node.event.memoryusage.SwapUsageEvent;
import io.watchcat.node.metrics.MemoryUsage;
import io.watchcat.node.metrics.reporting.LinuxMetricsCollector;
import io.watchcat.node.monitoring.threshold.MemoryUsageThresholds;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Monitors {@link MemoryUsage}
 *
 * @author Thomas Cashman
 */
@Component
public class MemoryUsageMonitor implements Runnable {
	public static final int INTERVAL = 5;
	
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
		scheduledExecutorService.schedule(this, INTERVAL + 1, TimeUnit.SECONDS);
	}

	@Override
	public void run() {
		try {
			MemoryUsage memoryUsage = metricsCollector.getMemoryUsage();
			checkRAMUsage(memoryUsage);
			checkSwapUsage(memoryUsage);
		} catch (Exception e) {
			e.printStackTrace();
		}
		scheduledExecutorService.schedule(this, INTERVAL, TimeUnit.SECONDS);
	}

	private void checkRAMUsage(MemoryUsage memoryUsage) {
		double used = memoryUsage.getUsedMemory();
		double total = memoryUsage.getTotalMemory();
		double ramUsed = (used / total) * 100;
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
	
	private void checkSwapUsage(MemoryUsage memoryUsage) {
		double used = memoryUsage.getUsedSwap();
		double total = memoryUsage.getTotalSwap();
		double swapUsed = (used / total) * 100;
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

	public RAMUsageEvent getRamUsageEvent() {
		return ramUsageEvent;
	}

	public void setRamUsageEvent(RAMUsageEvent ramUsageEvent) {
		this.ramUsageEvent = ramUsageEvent;
	}

	public SwapUsageEvent getSwapUsageEvent() {
		return swapUsageEvent;
	}

	public void setSwapUsageEvent(SwapUsageEvent swapUsageEvent) {
		this.swapUsageEvent = swapUsageEvent;
	}

	public void setMetricsCollector(LinuxMetricsCollector metricsCollector) {
		this.metricsCollector = metricsCollector;
	}

	public void setScheduledExecutorService(
			ScheduledExecutorService scheduledExecutorService) {
		this.scheduledExecutorService = scheduledExecutorService;
	}

	public void setAlertSender(AlertSender alertSender) {
		this.alertSender = alertSender;
	}

	public void setMemoryUsageThresholds(MemoryUsageThresholds memoryUsageThresholds) {
		this.memoryUsageThresholds = memoryUsageThresholds;
	}
}
