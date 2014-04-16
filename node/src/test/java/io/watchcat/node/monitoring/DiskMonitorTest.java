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
import io.watchcat.node.monitoring.DiskMonitor;
import io.watchcat.node.monitoring.threshold.DiskThresholds;

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
 * Unit and integration tests for {@link DiskMonitor}
 *
 * @author Thomas Cashman
 */
public class DiskMonitorTest {
	private static final String DISK_NAME = "/dev/sdb";
	private static final String DISK_MOUNT_POINT = "/";
	
	private DiskMonitor diskMonitor;
	private Mockery mockery;

	private LinuxMetricsCollector metricsCollector;
	private ScheduledExecutorService scheduledExecutorService;
	private AlertSender alertSender;

	private DiskThresholds diskThresholds;
	private DiskUsage diskUsage;
	private Disk disk;

	@Before
	public void setup() {
		diskThresholds = new DiskThresholds();
		diskUsage = new DiskUsage();
		disk = new Disk(DISK_NAME, 100, 1, 99, 1, DISK_MOUNT_POINT);
		
		diskMonitor = new DiskMonitor();
		mockery = new Mockery();
		mockery.setImposteriser(ClassImposteriser.INSTANCE);

		metricsCollector = mockery.mock(LinuxMetricsCollector.class);
		scheduledExecutorService = mockery.mock(ScheduledExecutorService.class);
		alertSender = mockery.mock(AlertSender.class);

		diskMonitor.setAlertSender(alertSender);
		diskMonitor.setMetricsCollector(metricsCollector);
		diskMonitor.setDiskThresholds(diskThresholds);
		diskMonitor.setScheduledExecutorService(scheduledExecutorService);
	}

	@After
	public void teardown() {
		mockery.assertIsSatisfied();
	}

	@Test
	public void testPostConstruct() {
		mockery.checking(new Expectations() {
			{
				oneOf(scheduledExecutorService).schedule(diskMonitor,
						DiskMonitor.INTERVAL + 1, TimeUnit.SECONDS);
			}
		});
		diskMonitor.postConstruct();
	}

	@Test
	public void testRunWithNoEvents() {
		diskUsage.getDisks().add(disk);
		mockery.checking(new Expectations() {
			{
				oneOf(metricsCollector).getDiskUsage();
				will(returnValue(diskUsage));
				
				oneOf(scheduledExecutorService).schedule(diskMonitor,
						DiskMonitor.INTERVAL, TimeUnit.SECONDS);
			}
		});
		diskMonitor.run();
	}

	@Test
	public void testNewMinorEvent() {
		disk = new Disk(DISK_NAME, 100, 85, 15, 85, DISK_MOUNT_POINT);
		diskUsage.getDisks().add(disk);
		
		mockery.checking(new Expectations() {
			{
				oneOf(metricsCollector).getDiskUsage();
				will(returnValue(diskUsage));
				
				oneOf(alertSender).sendAlert(with(Criticality.MINOR), with(any(String.class)));
				
				oneOf(scheduledExecutorService).schedule(diskMonitor,
						DiskMonitor.INTERVAL, TimeUnit.SECONDS);
			}
		});
		diskMonitor.run();
		
		CriticalityEvent event = diskMonitor.getDiskUsageEvent(DISK_NAME);
		Assert.assertEquals(true, event != null);
	}

	@Test
	public void testNewMajorEvent() {
		disk = new Disk(DISK_NAME, 100, 90, 10, 90, DISK_MOUNT_POINT);
		diskUsage.getDisks().add(disk);
		
		mockery.checking(new Expectations() {
			{
				oneOf(metricsCollector).getDiskUsage();
				will(returnValue(diskUsage));
				
				oneOf(alertSender).sendAlert(with(Criticality.MAJOR), with(any(String.class)));
				
				oneOf(scheduledExecutorService).schedule(diskMonitor,
						DiskMonitor.INTERVAL, TimeUnit.SECONDS);
			}
		});
		diskMonitor.run();
		
		CriticalityEvent event = diskMonitor.getDiskUsageEvent(DISK_NAME);
		Assert.assertEquals(true, event != null);
	}

	@Test
	public void testNewCriticalEvent() {
		disk = new Disk(DISK_NAME, 100, 95, 5, 95, DISK_MOUNT_POINT);
		diskUsage.getDisks().add(disk);
		
		mockery.checking(new Expectations() {
			{
				oneOf(metricsCollector).getDiskUsage();
				will(returnValue(diskUsage));
				
				oneOf(alertSender).sendAlert(with(Criticality.CRITICAL), with(any(String.class)));
				
				oneOf(scheduledExecutorService).schedule(diskMonitor,
						DiskMonitor.INTERVAL, TimeUnit.SECONDS);
			}
		});
		diskMonitor.run();
		
		CriticalityEvent event = diskMonitor.getDiskUsageEvent(DISK_NAME);
		Assert.assertEquals(true, event != null);
	}
	
	@Test
	public void testClearEvent() {
		disk = new Disk(DISK_NAME, 100, 70, 30, 70, DISK_MOUNT_POINT);
		diskUsage.getDisks().add(disk);
		
		diskMonitor.setDiskUsageEvent(DISK_NAME, new DiskUsageEvent(alertSender, DISK_NAME, Criticality.MINOR));
		
		mockery.checking(new Expectations() {
			{
				oneOf(metricsCollector).getDiskUsage();
				will(returnValue(diskUsage));
				
				oneOf(alertSender).sendAlert(with(Criticality.CLEAR), with(any(String.class)));
				
				oneOf(scheduledExecutorService).schedule(diskMonitor,
						DiskMonitor.INTERVAL, TimeUnit.SECONDS);
			}
		});
		diskMonitor.run();
		
		CriticalityEvent event = diskMonitor.getDiskUsageEvent(DISK_NAME);
		Assert.assertEquals(true, event == null);		
	}

	@Test
	public void testDowngradeToMinorEvent() {
		disk = new Disk(DISK_NAME, 100, 85, 15, 85, DISK_MOUNT_POINT);
		diskUsage.getDisks().add(disk);
		
		DiskUsageEvent diskUsageEvent = new DiskUsageEvent(alertSender, DISK_NAME, Criticality.MAJOR);
		diskMonitor.setDiskUsageEvent(DISK_NAME, diskUsageEvent);
		
		mockery.checking(new Expectations() {
			{
				oneOf(metricsCollector).getDiskUsage();
				will(returnValue(diskUsage));
				
				oneOf(alertSender).sendAlert(with(Criticality.MINOR), with(any(String.class)));
				
				oneOf(scheduledExecutorService).schedule(diskMonitor,
						DiskMonitor.INTERVAL, TimeUnit.SECONDS);
			}
		});
		diskMonitor.run();
		
		CriticalityEvent event = diskMonitor.getDiskUsageEvent(DISK_NAME);
		Assert.assertEquals(diskUsageEvent, event);
	}

	@Test
	public void testDowngradeToMajorEvent() {
		disk = new Disk(DISK_NAME, 100, 90, 10, 90, DISK_MOUNT_POINT);
		diskUsage.getDisks().add(disk);
		
		DiskUsageEvent diskUsageEvent = new DiskUsageEvent(alertSender, DISK_NAME, Criticality.CRITICAL);
		diskMonitor.setDiskUsageEvent(DISK_NAME, diskUsageEvent);
		
		mockery.checking(new Expectations() {
			{
				oneOf(metricsCollector).getDiskUsage();
				will(returnValue(diskUsage));
				
				oneOf(alertSender).sendAlert(with(Criticality.MAJOR), with(any(String.class)));
				
				oneOf(scheduledExecutorService).schedule(diskMonitor,
						DiskMonitor.INTERVAL, TimeUnit.SECONDS);
			}
		});
		diskMonitor.run();
		
		CriticalityEvent event = diskMonitor.getDiskUsageEvent(DISK_NAME);
		Assert.assertEquals(diskUsageEvent, event);
	}

	@Test
	public void testUpgradeToMajorEvent() {
		disk = new Disk(DISK_NAME, 100, 90, 10, 90, DISK_MOUNT_POINT);
		diskUsage.getDisks().add(disk);
		
		DiskUsageEvent diskUsageEvent = new DiskUsageEvent(alertSender, DISK_NAME, Criticality.MINOR);
		diskMonitor.setDiskUsageEvent(DISK_NAME, diskUsageEvent);
		
		mockery.checking(new Expectations() {
			{
				oneOf(metricsCollector).getDiskUsage();
				will(returnValue(diskUsage));
				
				oneOf(alertSender).sendAlert(with(Criticality.MAJOR), with(any(String.class)));
				
				oneOf(scheduledExecutorService).schedule(diskMonitor,
						DiskMonitor.INTERVAL, TimeUnit.SECONDS);
			}
		});
		diskMonitor.run();
		
		CriticalityEvent event = diskMonitor.getDiskUsageEvent(DISK_NAME);
		Assert.assertEquals(diskUsageEvent, event);
	}

	@Test
	public void testUpgadeToCriticalEvent() {
		disk = new Disk(DISK_NAME, 100, 95, 5, 95, DISK_MOUNT_POINT);
		diskUsage.getDisks().add(disk);
		
		DiskUsageEvent diskUsageEvent = new DiskUsageEvent(alertSender, DISK_NAME, Criticality.MAJOR);
		diskMonitor.setDiskUsageEvent(DISK_NAME, diskUsageEvent);
		
		mockery.checking(new Expectations() {
			{
				oneOf(metricsCollector).getDiskUsage();
				will(returnValue(diskUsage));
				
				oneOf(alertSender).sendAlert(with(Criticality.CRITICAL), with(any(String.class)));
				
				oneOf(scheduledExecutorService).schedule(diskMonitor,
						DiskMonitor.INTERVAL, TimeUnit.SECONDS);
			}
		});
		diskMonitor.run();
		
		CriticalityEvent event = diskMonitor.getDiskUsageEvent(DISK_NAME);
		Assert.assertEquals(diskUsageEvent, event);
	}
}
