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
package com.viridiansoftware.watchcat.node.event.diskusage;

import com.viridiansoftware.watchcat.node.event.Criticality;
import com.viridiansoftware.watchcat.node.event.CriticalityEvent;
import com.viridiansoftware.watchcat.node.metrics.DiskUsage;
import com.viridiansoftware.watchcat.node.metrics.domain.Filesystem;

/**
 * Stores the event status for the {@link DiskUsage} of a specific
 * {@link Filesystem} and sends associated alerts
 *
 * @author Thomas Cashman
 */
public class DiskUsageEvent implements CriticalityEvent {
	private String filesystem;
	private Criticality criticality;
	private String percentageUsed;

	public DiskUsageEvent(String filesystem) {
		this.filesystem = filesystem;
	}

	@Override
	public void begin(Criticality criticality, String... eventParams) {
		this.criticality = criticality;
		this.percentageUsed = eventParams[0];
		sendAlert();
	}

	@Override
	public void end(String... eventParams) {
		this.criticality = Criticality.CLEAR;
		sendAlert();
	}

	@Override
	public void updateStatus(Criticality criticality, String... eventParams) {
		if (this.criticality != criticality) {
			this.criticality = criticality;
			this.percentageUsed = eventParams[0];
			sendAlert();
		}
	}

	@Override
	public void sendAlert() {
		String alertMessage;
		switch (criticality) {
		case CLEAR:
			alertMessage = "Filesystem '" + filesystem
					+ "' usage has returned to normal";
			break;
		default:
			alertMessage = "Filesystem '" + filesystem + "' has reached a "
					+ criticality + " level of " + percentageUsed + "% used";
			break;
		}
	}
}
