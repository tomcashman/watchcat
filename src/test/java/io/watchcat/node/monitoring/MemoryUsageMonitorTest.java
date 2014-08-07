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
import io.watchcat.node.event.RAMUsageEvent;
import io.watchcat.node.event.SwapUsageEvent;
import io.watchcat.node.metrics.MemoryUsage;
import io.watchcat.node.monitoring.MemoryUsageMonitor;
import io.watchcat.node.monitoring.threshold.MemoryUsageThresholds;
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
 * Unit and integration tests for {@link MemoryUsageMonitor}
 * 
 * @author Thomas Cashman
 */
public class MemoryUsageMonitorTest {

	private MemoryUsageMonitor memoryUsageMonitor;
	private Mockery mockery;

	private LinuxMetricsCollector metricsCollector;
	private ScheduledExecutorService scheduledExecutorService;
	private AlertSender alertSender;
	
	private MemoryUsageThresholds memoryUsageThresholds;
	private MemoryUsage memoryUsage;
	
	@Before
	public void setup() {
		memoryUsage = new MemoryUsage();
		memoryUsage.setTotalMemory(100);
		memoryUsage.setTotalSwap(100);
		
		memoryUsageThresholds = new MemoryUsageThresholds();
		
		memoryUsageMonitor = new MemoryUsageMonitor();
		mockery = new Mockery();
		mockery.setImposteriser(ClassImposteriser.INSTANCE);

		metricsCollector = mockery.mock(LinuxMetricsCollector.class);
		scheduledExecutorService = mockery.mock(ScheduledExecutorService.class);
		alertSender = mockery.mock(AlertSender.class);

		memoryUsageMonitor.setAlertSender(alertSender);
		memoryUsageMonitor.setMetricsCollector(metricsCollector);
		memoryUsageMonitor.setScheduledExecutorService(scheduledExecutorService);
		memoryUsageMonitor.setMemoryUsageThresholds(memoryUsageThresholds);
	}

	@After
	public void teardown() {
		mockery.assertIsSatisfied();
	}
	
	@Test
	public void testPostConstruct() {
		mockery.checking(new Expectations() {
			{
				oneOf(scheduledExecutorService).schedule(memoryUsageMonitor,
						MemoryUsageMonitor.INTERVAL + 1, TimeUnit.SECONDS);
			}
		});
		memoryUsageMonitor.postConstruct();
	}
	
	@Test
	public void testRunWithNoEvents() {
		mockery.checking(new Expectations() {
			{
				oneOf(metricsCollector).getMemoryUsage();
				will(returnValue(memoryUsage));
				
				oneOf(scheduledExecutorService).schedule(memoryUsageMonitor,
						MemoryUsageMonitor.INTERVAL, TimeUnit.SECONDS);
			}
		});
		memoryUsageMonitor.run();
	}
	
	@Test
	public void testNewMinorMemoryEvent() {
		memoryUsage.setUsedMemory(80);
		
		mockery.checking(new Expectations() {
			{
				oneOf(metricsCollector).getMemoryUsage();
				will(returnValue(memoryUsage));
				
				oneOf(alertSender).sendAlert(with(Criticality.MINOR), with(any(String.class)));
				
				oneOf(scheduledExecutorService).schedule(memoryUsageMonitor,
						MemoryUsageMonitor.INTERVAL, TimeUnit.SECONDS);
			}
		});
		memoryUsageMonitor.run();
		
		Assert.assertEquals(true, memoryUsageMonitor.getRamUsageEvent() != null);
		Assert.assertEquals(true, memoryUsageMonitor.getSwapUsageEvent() == null);
	}
	
	@Test
	public void testNewMajorMemoryEvent() {
		memoryUsage.setUsedMemory(90);
		
		mockery.checking(new Expectations() {
			{
				oneOf(metricsCollector).getMemoryUsage();
				will(returnValue(memoryUsage));
				
				oneOf(alertSender).sendAlert(with(Criticality.MAJOR), with(any(String.class)));
				
				oneOf(scheduledExecutorService).schedule(memoryUsageMonitor,
						MemoryUsageMonitor.INTERVAL, TimeUnit.SECONDS);
			}
		});
		memoryUsageMonitor.run();
		
		Assert.assertEquals(true, memoryUsageMonitor.getRamUsageEvent() != null);
		Assert.assertEquals(true, memoryUsageMonitor.getSwapUsageEvent() == null);
	}
	
	@Test
	public void testNewCriticalMemoryEvent() {
		memoryUsage.setUsedMemory(95);
		
		mockery.checking(new Expectations() {
			{
				oneOf(metricsCollector).getMemoryUsage();
				will(returnValue(memoryUsage));
				
				oneOf(alertSender).sendAlert(with(Criticality.CRITICAL), with(any(String.class)));
				
				oneOf(scheduledExecutorService).schedule(memoryUsageMonitor,
						MemoryUsageMonitor.INTERVAL, TimeUnit.SECONDS);
			}
		});
		memoryUsageMonitor.run();
		
		Assert.assertEquals(true, memoryUsageMonitor.getRamUsageEvent() != null);
		Assert.assertEquals(true, memoryUsageMonitor.getSwapUsageEvent() == null);
	}
	
	@Test
	public void testNewMinorSwapEvent() {
		memoryUsage.setUsedSwap(80);
		
		mockery.checking(new Expectations() {
			{
				oneOf(metricsCollector).getMemoryUsage();
				will(returnValue(memoryUsage));
				
				oneOf(alertSender).sendAlert(with(Criticality.MINOR), with(any(String.class)));
				
				oneOf(scheduledExecutorService).schedule(memoryUsageMonitor,
						MemoryUsageMonitor.INTERVAL, TimeUnit.SECONDS);
			}
		});
		memoryUsageMonitor.run();
		
		Assert.assertEquals(true, memoryUsageMonitor.getRamUsageEvent() == null);
		Assert.assertEquals(true, memoryUsageMonitor.getSwapUsageEvent() != null);
	}
	
	@Test
	public void testNewMajorSwapEvent() {
		memoryUsage.setUsedSwap(90);
		
		mockery.checking(new Expectations() {
			{
				oneOf(metricsCollector).getMemoryUsage();
				will(returnValue(memoryUsage));
				
				oneOf(alertSender).sendAlert(with(Criticality.MAJOR), with(any(String.class)));
				
				oneOf(scheduledExecutorService).schedule(memoryUsageMonitor,
						MemoryUsageMonitor.INTERVAL, TimeUnit.SECONDS);
			}
		});
		memoryUsageMonitor.run();
		
		Assert.assertEquals(true, memoryUsageMonitor.getRamUsageEvent() == null);
		Assert.assertEquals(true, memoryUsageMonitor.getSwapUsageEvent() != null);
	}
	
	@Test
	public void testNewCriticalSwapEvent() {
		memoryUsage.setUsedSwap(95);
		
		mockery.checking(new Expectations() {
			{
				oneOf(metricsCollector).getMemoryUsage();
				will(returnValue(memoryUsage));
				
				oneOf(alertSender).sendAlert(with(Criticality.CRITICAL), with(any(String.class)));
				
				oneOf(scheduledExecutorService).schedule(memoryUsageMonitor,
						MemoryUsageMonitor.INTERVAL, TimeUnit.SECONDS);
			}
		});
		memoryUsageMonitor.run();
		
		Assert.assertEquals(true, memoryUsageMonitor.getRamUsageEvent() == null);
		Assert.assertEquals(true, memoryUsageMonitor.getSwapUsageEvent() != null);
	}
	
	@Test
	public void testClearMemoryEvent() {
		memoryUsage.setUsedMemory(79);
		
		memoryUsageMonitor.setRamUsageEvent(new RAMUsageEvent(alertSender, Criticality.MINOR));
		
		mockery.checking(new Expectations() {
			{
				oneOf(metricsCollector).getMemoryUsage();
				will(returnValue(memoryUsage));
				
				oneOf(alertSender).sendAlert(with(Criticality.CLEAR), with(any(String.class)));
				
				oneOf(scheduledExecutorService).schedule(memoryUsageMonitor,
						MemoryUsageMonitor.INTERVAL, TimeUnit.SECONDS);
			}
		});
		memoryUsageMonitor.run();
		
		Assert.assertEquals(true, memoryUsageMonitor.getRamUsageEvent() == null);
		Assert.assertEquals(true, memoryUsageMonitor.getSwapUsageEvent() == null);
	}
	
	@Test
	public void testClearSwapEvent() {
		memoryUsage.setUsedSwap(79);
		
		memoryUsageMonitor.setSwapUsageEvent(new SwapUsageEvent(alertSender, Criticality.MINOR));
		
		mockery.checking(new Expectations() {
			{
				oneOf(metricsCollector).getMemoryUsage();
				will(returnValue(memoryUsage));
				
				oneOf(alertSender).sendAlert(with(Criticality.CLEAR), with(any(String.class)));
				
				oneOf(scheduledExecutorService).schedule(memoryUsageMonitor,
						MemoryUsageMonitor.INTERVAL, TimeUnit.SECONDS);
			}
		});
		memoryUsageMonitor.run();
		
		Assert.assertEquals(true, memoryUsageMonitor.getRamUsageEvent() == null);
		Assert.assertEquals(true, memoryUsageMonitor.getSwapUsageEvent() == null);
	}
	
	@Test
	public void testUpgradeToMajorMemoryEvent() {
		memoryUsage.setUsedMemory(90);
		
		memoryUsageMonitor.setRamUsageEvent(new RAMUsageEvent(alertSender, Criticality.MINOR));
		
		mockery.checking(new Expectations() {
			{
				oneOf(metricsCollector).getMemoryUsage();
				will(returnValue(memoryUsage));
				
				oneOf(alertSender).sendAlert(with(Criticality.MAJOR), with(any(String.class)));
				
				oneOf(scheduledExecutorService).schedule(memoryUsageMonitor,
						MemoryUsageMonitor.INTERVAL, TimeUnit.SECONDS);
			}
		});
		memoryUsageMonitor.run();
		
		Assert.assertEquals(true, memoryUsageMonitor.getRamUsageEvent() != null);
		Assert.assertEquals(true, memoryUsageMonitor.getSwapUsageEvent() == null);
	}
	
	@Test
	public void testUpgradeToCriticalMemoryEvent() {
		memoryUsage.setUsedMemory(95);
		
		RAMUsageEvent event = new RAMUsageEvent(alertSender, Criticality.MAJOR);
		memoryUsageMonitor.setRamUsageEvent(event);
		
		mockery.checking(new Expectations() {
			{
				oneOf(metricsCollector).getMemoryUsage();
				will(returnValue(memoryUsage));
				
				oneOf(alertSender).sendAlert(with(Criticality.CRITICAL), with(any(String.class)));
				
				oneOf(scheduledExecutorService).schedule(memoryUsageMonitor,
						MemoryUsageMonitor.INTERVAL, TimeUnit.SECONDS);
			}
		});
		memoryUsageMonitor.run();
		
		Assert.assertEquals(event, memoryUsageMonitor.getRamUsageEvent());
		Assert.assertEquals(true, memoryUsageMonitor.getSwapUsageEvent() == null);
	}
	
	@Test
	public void testDowngradeToMajorMemoryEvent() {
		memoryUsage.setUsedMemory(90);
		
		RAMUsageEvent event = new RAMUsageEvent(alertSender, Criticality.CRITICAL);
		memoryUsageMonitor.setRamUsageEvent(event);
		
		mockery.checking(new Expectations() {
			{
				oneOf(metricsCollector).getMemoryUsage();
				will(returnValue(memoryUsage));
				
				oneOf(alertSender).sendAlert(with(Criticality.MAJOR), with(any(String.class)));
				
				oneOf(scheduledExecutorService).schedule(memoryUsageMonitor,
						MemoryUsageMonitor.INTERVAL, TimeUnit.SECONDS);
			}
		});
		memoryUsageMonitor.run();
		
		Assert.assertEquals(event, memoryUsageMonitor.getRamUsageEvent());
		Assert.assertEquals(true, memoryUsageMonitor.getSwapUsageEvent() == null);
	}
	
	@Test
	public void testDowngradeToMinorMemoryEvent() {
		memoryUsage.setUsedMemory(80);
		
		RAMUsageEvent event = new RAMUsageEvent(alertSender, Criticality.MAJOR);
		memoryUsageMonitor.setRamUsageEvent(event);
		
		mockery.checking(new Expectations() {
			{
				oneOf(metricsCollector).getMemoryUsage();
				will(returnValue(memoryUsage));
				
				oneOf(alertSender).sendAlert(with(Criticality.MINOR), with(any(String.class)));
				
				oneOf(scheduledExecutorService).schedule(memoryUsageMonitor,
						MemoryUsageMonitor.INTERVAL, TimeUnit.SECONDS);
			}
		});
		memoryUsageMonitor.run();
		
		Assert.assertEquals(event, memoryUsageMonitor.getRamUsageEvent());
		Assert.assertEquals(true, memoryUsageMonitor.getSwapUsageEvent() == null);
	}
}
