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
import io.watchcat.node.monitoring.LoadAverageMonitor;
import io.watchcat.node.monitoring.threshold.LoadAverageThresholds;
import io.watchcat.node.reporting.LinuxMetricsCollector;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit and integration tests for {@link LoadAverageMonitor} for 1 minute load average
 * 
 * @author Thomas Cashman
 */
public class OneMinuteLoadAverageMonitorTest {
	private static final double MINOR_THRESHOLD = 0.1;
	private static final double MAJOR_THRESHOLD = MINOR_THRESHOLD + 0.1;
	private static final double CRITICAL_THRESHOLD = MINOR_THRESHOLD + 0.2;

	private LoadAverageMonitor loadAverageMonitor;
	private Mockery mockery;

	private LinuxMetricsCollector metricsCollector;
	private ScheduledExecutorService scheduledExecutorService;
	private AlertSender alertSender;
	
	private LoadAverageThresholds loadAverageThresholds;
	private LoadAverage loadAverage;
	
	@Before
	public void setup() {
		loadAverageMonitor = new LoadAverageMonitor();
		loadAverageThresholds = new LoadAverageThresholds(MINOR_THRESHOLD);
		loadAverage = new LoadAverage();
		
		mockery = new Mockery();
		mockery.setImposteriser(ClassImposteriser.INSTANCE);
		
		metricsCollector = mockery.mock(LinuxMetricsCollector.class);
		scheduledExecutorService = mockery.mock(ScheduledExecutorService.class);
		alertSender = mockery.mock(AlertSender.class);
		
		loadAverageMonitor.setAlertSender(alertSender);
		loadAverageMonitor.setMetricsCollector(metricsCollector);
		loadAverageMonitor.setScheduledExecutorService(scheduledExecutorService);
		loadAverageMonitor.setLoadAverageThresholds(loadAverageThresholds);
	}
	
	@After
	public void teardown() {
		mockery.assertIsSatisfied();
	}
	
	@Test
	public void testPostConstruct() {
		mockery.checking(new Expectations() {
			{
				oneOf(scheduledExecutorService).schedule(loadAverageMonitor,
						LoadAverageMonitor.INTERVAL + 1, TimeUnit.SECONDS);
			}
		});
		loadAverageMonitor.postConstruct();
	}
	
	@Test
	public void testRunWithNoEvents() {
		loadAverage.setOneMinuteAverage(0.01);
		loadAverage.setFiveMinuteAverage(0.01);
		loadAverage.setFifteenMinuteAverage(0.01);
		
		mockery.checking(new Expectations() {
			{
				oneOf(metricsCollector).getLoadAverage();
				will(returnValue(loadAverage));
				
				oneOf(scheduledExecutorService).schedule(loadAverageMonitor,
						LoadAverageMonitor.INTERVAL, TimeUnit.SECONDS);
			}
		});
		loadAverageMonitor.run();
		
		Assert.assertEquals(true, loadAverageMonitor.getOneMinuteAverageEvent() == null);
		Assert.assertEquals(true, loadAverageMonitor.getFiveMinuteAverageEvent() == null);
		Assert.assertEquals(true, loadAverageMonitor.getFifteenMinuteAverageEvent() == null);
	}
	
	@Test
	public void testNewOneMinuteMinorEvent() {
		loadAverage.setOneMinuteAverage(MINOR_THRESHOLD);
		loadAverage.setFiveMinuteAverage(0.01);
		loadAverage.setFifteenMinuteAverage(0.01);
		
		mockery.checking(new Expectations() {
			{
				oneOf(metricsCollector).getLoadAverage();
				will(returnValue(loadAverage));
				
				oneOf(alertSender).sendAlert(with(Criticality.MINOR), with(any(String.class)));
				
				oneOf(scheduledExecutorService).schedule(loadAverageMonitor,
						LoadAverageMonitor.INTERVAL, TimeUnit.SECONDS);
			}
		});
		loadAverageMonitor.run();
		
		Assert.assertEquals(true, loadAverageMonitor.getOneMinuteAverageEvent() != null);
		Assert.assertEquals(true, loadAverageMonitor.getFiveMinuteAverageEvent() == null);
		Assert.assertEquals(true, loadAverageMonitor.getFifteenMinuteAverageEvent() == null);
	}
	
	@Test
	public void testNewOneMinuteMajorEvent() {
		loadAverage.setOneMinuteAverage(MAJOR_THRESHOLD);
		loadAverage.setFiveMinuteAverage(0.01);
		loadAverage.setFifteenMinuteAverage(0.01);
		
		mockery.checking(new Expectations() {
			{
				oneOf(metricsCollector).getLoadAverage();
				will(returnValue(loadAverage));
				
				oneOf(alertSender).sendAlert(with(Criticality.MAJOR), with(any(String.class)));
				
				oneOf(scheduledExecutorService).schedule(loadAverageMonitor,
						LoadAverageMonitor.INTERVAL, TimeUnit.SECONDS);
			}
		});
		loadAverageMonitor.run();
		
		Assert.assertEquals(true, loadAverageMonitor.getOneMinuteAverageEvent() != null);
		Assert.assertEquals(true, loadAverageMonitor.getFiveMinuteAverageEvent() == null);
		Assert.assertEquals(true, loadAverageMonitor.getFifteenMinuteAverageEvent() == null);

	}
	
	@Test
	public void testNewOneMinuteCriticalEvent() {
		loadAverage.setOneMinuteAverage(CRITICAL_THRESHOLD);
		loadAverage.setFiveMinuteAverage(0.01);
		loadAverage.setFifteenMinuteAverage(0.01);
		
		mockery.checking(new Expectations() {
			{
				oneOf(metricsCollector).getLoadAverage();
				will(returnValue(loadAverage));
				
				oneOf(alertSender).sendAlert(with(Criticality.CRITICAL), with(any(String.class)));
				
				oneOf(scheduledExecutorService).schedule(loadAverageMonitor,
						LoadAverageMonitor.INTERVAL, TimeUnit.SECONDS);
			}
		});
		loadAverageMonitor.run();
		
		Assert.assertEquals(true, loadAverageMonitor.getOneMinuteAverageEvent() != null);
		Assert.assertEquals(true, loadAverageMonitor.getFiveMinuteAverageEvent() == null);
		Assert.assertEquals(true, loadAverageMonitor.getFifteenMinuteAverageEvent() == null);
	}
	
	@Test
	public void testClearOneMinuteEvent() {
		loadAverage.setOneMinuteAverage(0.01);
		loadAverage.setFiveMinuteAverage(0.01);
		loadAverage.setFifteenMinuteAverage(0.01);
		
		loadAverageMonitor.setOneMinuteAverageEvent(new LoadAverageEvent(alertSender, 1, Criticality.MINOR));
		
		mockery.checking(new Expectations() {
			{
				oneOf(metricsCollector).getLoadAverage();
				will(returnValue(loadAverage));
				
				oneOf(alertSender).sendAlert(with(Criticality.CLEAR), with(any(String.class)));
				
				oneOf(scheduledExecutorService).schedule(loadAverageMonitor,
						LoadAverageMonitor.INTERVAL, TimeUnit.SECONDS);
			}
		});
		loadAverageMonitor.run();
		
		Assert.assertEquals(true, loadAverageMonitor.getOneMinuteAverageEvent() == null);
		Assert.assertEquals(true, loadAverageMonitor.getFiveMinuteAverageEvent() == null);
		Assert.assertEquals(true, loadAverageMonitor.getFifteenMinuteAverageEvent() == null);	
	}
	
	@Test
	public void testUpgradeToMajorEvent() {
		loadAverage.setOneMinuteAverage(MAJOR_THRESHOLD);
		loadAverage.setFiveMinuteAverage(0.01);
		loadAverage.setFifteenMinuteAverage(0.01);
		
		LoadAverageEvent loadAverageEvent = new LoadAverageEvent(alertSender, 1, Criticality.MINOR);
		loadAverageMonitor.setOneMinuteAverageEvent(loadAverageEvent);
		
		mockery.checking(new Expectations() {
			{
				oneOf(metricsCollector).getLoadAverage();
				will(returnValue(loadAverage));
				
				oneOf(alertSender).sendAlert(with(Criticality.MAJOR), with(any(String.class)));
				
				oneOf(scheduledExecutorService).schedule(loadAverageMonitor,
						LoadAverageMonitor.INTERVAL, TimeUnit.SECONDS);
			}
		});
		loadAverageMonitor.run();
		
		Assert.assertEquals(true, loadAverageMonitor.getOneMinuteAverageEvent() != null);
		Assert.assertEquals(true, loadAverageMonitor.getFiveMinuteAverageEvent() == null);
		Assert.assertEquals(true, loadAverageMonitor.getFifteenMinuteAverageEvent() == null);			
	}
	
	@Test
	public void testUpgradeToCriticalEvent() {
		loadAverage.setOneMinuteAverage(CRITICAL_THRESHOLD);
		loadAverage.setFiveMinuteAverage(0.01);
		loadAverage.setFifteenMinuteAverage(0.01);
		
		LoadAverageEvent loadAverageEvent = new LoadAverageEvent(alertSender, 1, Criticality.MINOR);
		loadAverageMonitor.setOneMinuteAverageEvent(loadAverageEvent);
		
		mockery.checking(new Expectations() {
			{
				oneOf(metricsCollector).getLoadAverage();
				will(returnValue(loadAverage));
				
				oneOf(alertSender).sendAlert(with(Criticality.CRITICAL), with(any(String.class)));
				
				oneOf(scheduledExecutorService).schedule(loadAverageMonitor,
						LoadAverageMonitor.INTERVAL, TimeUnit.SECONDS);
			}
		});
		loadAverageMonitor.run();
		
		Assert.assertEquals(loadAverageEvent, loadAverageMonitor.getOneMinuteAverageEvent());
		Assert.assertEquals(true, loadAverageMonitor.getFiveMinuteAverageEvent() == null);
		Assert.assertEquals(true, loadAverageMonitor.getFifteenMinuteAverageEvent() == null);				
	}
	
	@Test
	public void testDowngradeToMajorEvent() {
		loadAverage.setOneMinuteAverage(MAJOR_THRESHOLD);
		loadAverage.setFiveMinuteAverage(0.01);
		loadAverage.setFifteenMinuteAverage(0.01);
		
		LoadAverageEvent loadAverageEvent = new LoadAverageEvent(alertSender, 1, Criticality.CRITICAL);
		loadAverageMonitor.setOneMinuteAverageEvent(loadAverageEvent);
		
		mockery.checking(new Expectations() {
			{
				oneOf(metricsCollector).getLoadAverage();
				will(returnValue(loadAverage));
				
				oneOf(alertSender).sendAlert(with(Criticality.MAJOR), with(any(String.class)));
				
				oneOf(scheduledExecutorService).schedule(loadAverageMonitor,
						LoadAverageMonitor.INTERVAL, TimeUnit.SECONDS);
			}
		});
		loadAverageMonitor.run();
		
		Assert.assertEquals(loadAverageEvent, loadAverageMonitor.getOneMinuteAverageEvent());
		Assert.assertEquals(true, loadAverageMonitor.getFiveMinuteAverageEvent() == null);
		Assert.assertEquals(true, loadAverageMonitor.getFifteenMinuteAverageEvent() == null);			
	}
	
	@Test
	public void testDowngradeToMinorEvent() {
		loadAverage.setOneMinuteAverage(MINOR_THRESHOLD);
		loadAverage.setFiveMinuteAverage(0.01);
		loadAverage.setFifteenMinuteAverage(0.01);
		
		LoadAverageEvent loadAverageEvent = new LoadAverageEvent(alertSender, 1, Criticality.MAJOR);
		loadAverageMonitor.setOneMinuteAverageEvent(loadAverageEvent);
		
		mockery.checking(new Expectations() {
			{
				oneOf(metricsCollector).getLoadAverage();
				will(returnValue(loadAverage));
				
				oneOf(alertSender).sendAlert(with(Criticality.MINOR), with(any(String.class)));
				
				oneOf(scheduledExecutorService).schedule(loadAverageMonitor,
						LoadAverageMonitor.INTERVAL, TimeUnit.SECONDS);
			}
		});
		loadAverageMonitor.run();
		
		Assert.assertEquals(loadAverageEvent, loadAverageMonitor.getOneMinuteAverageEvent());
		Assert.assertEquals(true, loadAverageMonitor.getFiveMinuteAverageEvent() == null);
		Assert.assertEquals(true, loadAverageMonitor.getFifteenMinuteAverageEvent() == null);			
	}
}
