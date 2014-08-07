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
package io.watchcat.node.event;

import io.watchcat.node.alerts.AlertSender;
import io.watchcat.node.metrics.DiskUsage;
import io.watchcat.node.metrics.domain.Disk;

/**
 * Stores the event status for the {@link DiskUsage} of a specific
 * {@link Disk} and sends associated alerts
 *
 * @author Thomas Cashman
 */
public class DiskUsageEvent implements CriticalityEvent {
	private String disk;
	private Criticality criticality;
	private String percentageUsed;
	private AlertSender alertSender;

	public DiskUsageEvent(AlertSender alertSender, String disk) {
		this(alertSender, disk, null);
	}
	
	public DiskUsageEvent(AlertSender alertSender, String disk, Criticality criticality) {
		this.disk = disk;
		this.alertSender = alertSender;
		this.criticality = criticality;
	}

	@Override
	public void begin(Criticality criticality, String... eventParams) {
		this.criticality = criticality;
		this.percentageUsed = eventParams[0];
		alert();
	}

	@Override
	public void end(String... eventParams) {
		this.criticality = Criticality.CLEAR;
		alert();
	}

	@Override
	public void updateStatus(Criticality criticality, String... eventParams) {
		if (this.criticality != criticality) {
			this.criticality = criticality;
			this.percentageUsed = eventParams[0];
			alert();
		}
	}

	@Override
	public void alert() {
		String alertMessage;
		switch (criticality) {
		case CLEAR:
			alertMessage = "Disk usage on '" + disk
					+ "' has returned to normal";
			break;
		default:
			alertMessage = "Disk '" + disk + "' is now at " + percentageUsed + "% used";
			break;
		}
		alertSender.sendAlert(criticality, alertMessage);
	}
}
