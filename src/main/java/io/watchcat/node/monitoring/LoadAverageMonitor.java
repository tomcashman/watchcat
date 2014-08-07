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
import io.watchcat.node.event.LoadAverageEvent;
import io.watchcat.node.metrics.LoadAverage;
import io.watchcat.node.monitoring.threshold.LoadAverageThresholds;
import io.watchcat.node.reporting.LinuxMetricsCollector;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Monitors {@link LoadAverage}
 *
 * @author Thomas Cashman
 */
@Component
public class LoadAverageMonitor implements Runnable {
	public static final int INTERVAL = 5;
	
	@Autowired
	private LinuxMetricsCollector metricsCollector;
	@Autowired
	private ScheduledExecutorService scheduledExecutorService;
	@Autowired
	private LoadAverageThresholds loadAverageThresholds;
	@Autowired
	private AlertSender alertSender;
	
	private LoadAverageEvent oneMinuteAverageEvent, fiveMinuteAverageEvent, fifteenMinuteAverageEvent;
	
	@PostConstruct
	public void postConstruct() {
		scheduledExecutorService.schedule(this, INTERVAL + 1, TimeUnit.SECONDS);
	}

	@Override
	public void run() {
		try {
			LoadAverage loadAverage = metricsCollector.getLoadAverage();
			checkOneMinuteAverage(loadAverage);
			checkFiveMinuteAverage(loadAverage);
			checkFifteenMinuteAverage(loadAverage);
		} catch (Exception e) {
			e.printStackTrace();
		}
		scheduledExecutorService.schedule(this, INTERVAL, TimeUnit.SECONDS);
	}

	private void checkOneMinuteAverage(LoadAverage loadAverage) {
		if(loadAverage.getOneMinuteAverage() >= loadAverageThresholds.getOneMinuteAverageCriticalThreshold()) {
			if(oneMinuteAverageEvent == null) {
				beginOneMinuteAverageEvent(Criticality.CRITICAL, loadAverage.getOneMinuteAverage());
			} else {
				oneMinuteAverageEvent.updateStatus(Criticality.CRITICAL, String.valueOf(loadAverage.getOneMinuteAverage()));
			}
		} else if(loadAverage.getOneMinuteAverage() >= loadAverageThresholds.getOneMinuteAverageMajorThreshold()) {
			if(oneMinuteAverageEvent == null) {
				beginOneMinuteAverageEvent(Criticality.MAJOR, loadAverage.getOneMinuteAverage());
			} else {
				oneMinuteAverageEvent.updateStatus(Criticality.MAJOR, String.valueOf(loadAverage.getOneMinuteAverage()));
			}
		} else if(loadAverage.getOneMinuteAverage() >= loadAverageThresholds.getOneMinuteAverageMinorThreshold()) {
			if(oneMinuteAverageEvent == null) {
				beginOneMinuteAverageEvent(Criticality.MINOR, loadAverage.getOneMinuteAverage());
			} else {
				oneMinuteAverageEvent.updateStatus(Criticality.MINOR, String.valueOf(loadAverage.getOneMinuteAverage()));
			}
		} else if(oneMinuteAverageEvent != null) {
			oneMinuteAverageEvent.end();
			oneMinuteAverageEvent = null;
		}
	}
	
	private void beginOneMinuteAverageEvent(Criticality criticality, double loadAverage) {
		oneMinuteAverageEvent = new LoadAverageEvent(alertSender, 1);
		oneMinuteAverageEvent.begin(criticality, String.valueOf(loadAverage));
	}
	
	private void checkFiveMinuteAverage(LoadAverage loadAverage) {
		if(loadAverage.getFiveMinuteAverage() >= loadAverageThresholds.getFiveMinuteAverageCriticalThreshold()) {
			if(fiveMinuteAverageEvent == null) {
				beginFiveMinuteAverageEvent(Criticality.CRITICAL, loadAverage.getFiveMinuteAverage());
			} else {
				fiveMinuteAverageEvent.updateStatus(Criticality.CRITICAL, String.valueOf(loadAverage.getFiveMinuteAverage()));
			}
		} else if(loadAverage.getFiveMinuteAverage() >= loadAverageThresholds.getFiveMinuteAverageMajorThreshold()) {
			if(fiveMinuteAverageEvent == null) {
				beginFiveMinuteAverageEvent(Criticality.MAJOR, loadAverage.getFiveMinuteAverage());
			} else {
				fiveMinuteAverageEvent.updateStatus(Criticality.MAJOR, String.valueOf(loadAverage.getFiveMinuteAverage()));
			}
		} else if(loadAverage.getFiveMinuteAverage() >= loadAverageThresholds.getFiveMinuteAverageMinorThreshold()) {
			if(fiveMinuteAverageEvent == null) {
				beginFiveMinuteAverageEvent(Criticality.MINOR, loadAverage.getFiveMinuteAverage());
			} else {
				fiveMinuteAverageEvent.updateStatus(Criticality.MINOR, String.valueOf(loadAverage.getFiveMinuteAverage()));				
			}
		} else if(fiveMinuteAverageEvent != null){
			fiveMinuteAverageEvent.end();
			fiveMinuteAverageEvent = null;
		}
	}
	
	private void beginFiveMinuteAverageEvent(Criticality criticality, double loadAverage) {
		fiveMinuteAverageEvent = new LoadAverageEvent(alertSender, 5);
		fiveMinuteAverageEvent.begin(criticality, String.valueOf(loadAverage));
	}
	
	private void checkFifteenMinuteAverage(LoadAverage loadAverage) {
		if(loadAverage.getFifteenMinuteAverage() >= loadAverageThresholds.getFifteenMinuteAverageCriticalThreshold()) {
			if(fifteenMinuteAverageEvent == null) {
				beginFifteenMinuteAverageEvent(Criticality.CRITICAL, loadAverage.getFifteenMinuteAverage());
			} else {
				fifteenMinuteAverageEvent.updateStatus(Criticality.CRITICAL, String.valueOf(loadAverage.getFifteenMinuteAverage()));
			}
		} else if(loadAverage.getFifteenMinuteAverage() >= loadAverageThresholds.getFifteenMinuteAverageMajorThreshold()) {
			if(fifteenMinuteAverageEvent == null) {
				beginFifteenMinuteAverageEvent(Criticality.MAJOR, loadAverage.getFifteenMinuteAverage());
			} else {
				fifteenMinuteAverageEvent.updateStatus(Criticality.MAJOR, String.valueOf(loadAverage.getFifteenMinuteAverage()));				
			}
		} else if(loadAverage.getFifteenMinuteAverage() >= loadAverageThresholds.getFifteenMinuteAverageMinorThreshold()) {
			if(fifteenMinuteAverageEvent == null) {
				beginFifteenMinuteAverageEvent(Criticality.MINOR, loadAverage.getFifteenMinuteAverage());
			} else {
				fifteenMinuteAverageEvent.updateStatus(Criticality.MINOR, String.valueOf(loadAverage.getFifteenMinuteAverage()));				
			}
		} else if(fifteenMinuteAverageEvent != null){
			fifteenMinuteAverageEvent.end();
			fifteenMinuteAverageEvent = null;
		}
	}
	
	private void beginFifteenMinuteAverageEvent(Criticality criticality, double loadAverage) {
		fifteenMinuteAverageEvent = new LoadAverageEvent(alertSender, 15);
		fifteenMinuteAverageEvent.begin(criticality, String.valueOf(loadAverage));
	}

	public void setMetricsCollector(LinuxMetricsCollector metricsCollector) {
		this.metricsCollector = metricsCollector;
	}

	public void setScheduledExecutorService(
			ScheduledExecutorService scheduledExecutorService) {
		this.scheduledExecutorService = scheduledExecutorService;
	}

	public void setLoadAverageThresholds(LoadAverageThresholds loadAverageThresholds) {
		this.loadAverageThresholds = loadAverageThresholds;
	}

	public void setAlertSender(AlertSender alertSender) {
		this.alertSender = alertSender;
	}

	public LoadAverageEvent getOneMinuteAverageEvent() {
		return oneMinuteAverageEvent;
	}

	public void setOneMinuteAverageEvent(LoadAverageEvent oneMinuteAverageEvent) {
		this.oneMinuteAverageEvent = oneMinuteAverageEvent;
	}

	public LoadAverageEvent getFiveMinuteAverageEvent() {
		return fiveMinuteAverageEvent;
	}

	public void setFiveMinuteAverageEvent(LoadAverageEvent fiveMinuteAverageEvent) {
		this.fiveMinuteAverageEvent = fiveMinuteAverageEvent;
	}

	public LoadAverageEvent getFifteenMinuteAverageEvent() {
		return fifteenMinuteAverageEvent;
	}

	public void setFifteenMinuteAverageEvent(
			LoadAverageEvent fifteenMinuteAverageEvent) {
		this.fifteenMinuteAverageEvent = fifteenMinuteAverageEvent;
	}
}
