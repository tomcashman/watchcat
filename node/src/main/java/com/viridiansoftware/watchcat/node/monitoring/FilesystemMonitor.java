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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.viridiansoftware.watchcat.node.alerts.AlertSender;
import com.viridiansoftware.watchcat.node.event.Criticality;
import com.viridiansoftware.watchcat.node.event.CriticalityEvent;
import com.viridiansoftware.watchcat.node.event.diskusage.DiskUsageEvent;
import com.viridiansoftware.watchcat.node.metrics.DiskUsage;
import com.viridiansoftware.watchcat.node.metrics.domain.Filesystem;
import com.viridiansoftware.watchcat.node.metrics.reporting.LinuxMetricsCollector;
import com.viridiansoftware.watchcat.node.monitoring.threshold.FilesystemThresholds;

/**
 * Monitors {@link Filesystem} disk space usage
 *
 * @author Thomas Cashman
 */
@Component
public class FilesystemMonitor implements Runnable {
	@Autowired
	private LinuxMetricsCollector metricsCollector;
	@Autowired
	private ScheduledExecutorService scheduledExecutorService;
	@Autowired
	private FilesystemThresholds filesystemThresholds;
	@Autowired
	private AlertSender alertSender;
	
	private Map<String, CriticalityEvent> diskUsageEvents;
	
	public FilesystemMonitor() {
		diskUsageEvents = new ConcurrentHashMap<String, CriticalityEvent>();
	}
	
	@PostConstruct
	public void postConstruct() {
		scheduledExecutorService.schedule(this, 6000, TimeUnit.MILLISECONDS);
	}

	@Override
	public void run() {
		try {
			DiskUsage diskUsage = metricsCollector.getDiskUsage();
			
			List<Filesystem> filesystems = diskUsage.getFilesystems();
			Iterator<Filesystem> iterator = filesystems.iterator();
			while(iterator.hasNext()) {
				Filesystem filesystem = iterator.next();
				String key = filesystem.getFilesystem();
				if(key.compareToIgnoreCase("none") == 0) {
					continue;
				}
				
				CriticalityEvent existingEvent = diskUsageEvents.get(key);
				
				if(filesystem.getPercentageUsed() >= filesystemThresholds.getCriticalThreshold()) {
					if(existingEvent == null) {
						beginEvent(key, Criticality.CRITICAL, filesystem.getPercentageUsed());
					} else {
						existingEvent.updateStatus(Criticality.CRITICAL, String.valueOf(filesystem.getPercentageUsed()));
					}
				} else if(filesystem.getPercentageUsed() >= filesystemThresholds.getMajorThreshold()) {
					if(existingEvent == null) {
						beginEvent(key, Criticality.MAJOR, filesystem.getPercentageUsed());
					} else {
						existingEvent.updateStatus(Criticality.MAJOR, String.valueOf(filesystem.getPercentageUsed()));
					}
				} else if(filesystem.getPercentageUsed() >= filesystemThresholds.getMinorThreshold()) {
					if(existingEvent == null) {
						beginEvent(key, Criticality.MINOR, filesystem.getPercentageUsed());
					} else {
						existingEvent.updateStatus(Criticality.MINOR, String.valueOf(filesystem.getPercentageUsed()));
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
		scheduledExecutorService.schedule(this, 5000, TimeUnit.MILLISECONDS);
	}

	private void beginEvent(String filesystem, Criticality criticality, int percentageUsed) {
		DiskUsageEvent event = new DiskUsageEvent(alertSender, filesystem);
		event.begin(criticality, String.valueOf(percentageUsed));
		diskUsageEvents.put(filesystem, event);
	}
}
