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
import io.watchcat.node.event.CriticalityEvent;
import io.watchcat.node.event.diskusage.DiskUsageEvent;
import io.watchcat.node.metrics.DiskUsage;
import io.watchcat.node.metrics.domain.Disk;
import io.watchcat.node.metrics.reporting.LinuxMetricsCollector;
import io.watchcat.node.monitoring.threshold.DiskThresholds;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Monitors {@link Disk} disk space usage
 *
 * @author Thomas Cashman
 */
@Component
public class DiskMonitor implements Runnable {
	public static final int INTERVAL = 5;
	
	@Autowired
	private LinuxMetricsCollector metricsCollector;
	@Autowired
	private ScheduledExecutorService scheduledExecutorService;
	@Autowired
	private DiskThresholds diskThresholds;
	@Autowired
	private AlertSender alertSender;
	
	private Map<String, CriticalityEvent> diskUsageEvents;
	
	public DiskMonitor() {
		diskUsageEvents = new ConcurrentHashMap<String, CriticalityEvent>();
	}
	
	@PostConstruct
	public void postConstruct() {
		scheduledExecutorService.schedule(this, INTERVAL + 1, TimeUnit.SECONDS);
	}

	@Override
	public void run() {
		try {
			DiskUsage diskUsage = metricsCollector.getDiskUsage();
			
			List<Disk> disks = diskUsage.getDisks();
			Iterator<Disk> iterator = disks.iterator();
			while(iterator.hasNext()) {
				Disk disk = iterator.next();
				String key = disk.getDisk();
				if(key.compareToIgnoreCase("none") == 0) {
					continue;
				}
				
				CriticalityEvent existingEvent = diskUsageEvents.get(key);
				
				if(disk.getPercentageUsed() >= diskThresholds.getCriticalThreshold()) {
					if(existingEvent == null) {
						beginEvent(key, Criticality.CRITICAL, disk.getPercentageUsed());
					} else {
						existingEvent.updateStatus(Criticality.CRITICAL, String.valueOf(disk.getPercentageUsed()));
					}
				} else if(disk.getPercentageUsed() >= diskThresholds.getMajorThreshold()) {
					if(existingEvent == null) {
						beginEvent(key, Criticality.MAJOR, disk.getPercentageUsed());
					} else {
						existingEvent.updateStatus(Criticality.MAJOR, String.valueOf(disk.getPercentageUsed()));
					}
				} else if(disk.getPercentageUsed() >= diskThresholds.getMinorThreshold()) {
					if(existingEvent == null) {
						beginEvent(key, Criticality.MINOR, disk.getPercentageUsed());
					} else {
						existingEvent.updateStatus(Criticality.MINOR, String.valueOf(disk.getPercentageUsed()));
					}
				} else {
					if(existingEvent != null) {
						existingEvent.end();
						diskUsageEvents.remove(key);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		scheduledExecutorService.schedule(this, 5, TimeUnit.SECONDS);
	}

	private void beginEvent(String filesystem, Criticality criticality, int percentageUsed) {
		DiskUsageEvent event = new DiskUsageEvent(alertSender, filesystem);
		event.begin(criticality, String.valueOf(percentageUsed));
		diskUsageEvents.put(filesystem, event);
	}
	
	protected CriticalityEvent getDiskUsageEvent(String disk) {
		return diskUsageEvents.get(disk);
	}
	
	protected void setDiskUsageEvent(String disk, CriticalityEvent event) {
		diskUsageEvents.put(disk, event);
	}

	protected void setMetricsCollector(LinuxMetricsCollector metricsCollector) {
		this.metricsCollector = metricsCollector;
	}

	protected void setScheduledExecutorService(
			ScheduledExecutorService scheduledExecutorService) {
		this.scheduledExecutorService = scheduledExecutorService;
	}

	protected void setDiskThresholds(DiskThresholds diskThresholds) {
		this.diskThresholds = diskThresholds;
	}

	protected void setAlertSender(AlertSender alertSender) {
		this.alertSender = alertSender;
	}
}
