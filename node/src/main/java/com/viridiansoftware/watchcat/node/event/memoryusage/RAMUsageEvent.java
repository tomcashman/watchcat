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
package com.viridiansoftware.watchcat.node.event.memoryusage;

import com.viridiansoftware.watchcat.node.alerts.AlertSender;
import com.viridiansoftware.watchcat.node.event.Criticality;
import com.viridiansoftware.watchcat.node.event.CriticalityEvent;


/**
 *
 *
 * @author Thomas Cashman
 */
public class RAMUsageEvent implements CriticalityEvent {
	private AlertSender alertSender;
	private Criticality criticality;
	private String ramUsed;
	
	public RAMUsageEvent(AlertSender alertSender) {
		this.alertSender = alertSender;
	}

	@Override
	public void begin(Criticality criticality, String... eventParams) {
		this.criticality = criticality;
		this.ramUsed = eventParams[0];
		alert();
	}

	@Override
	public void end(String... eventParams) {
		this.criticality = Criticality.CLEAR;
		alert();
	}

	@Override
	public void updateStatus(Criticality criticality, String... eventParams) {
		if(this.criticality != criticality) {
			this.criticality = criticality;
			this.ramUsed = eventParams[0];
			alert();
		}
	}

	@Override
	public void alert() {
		String alertMessage;
		switch (criticality) {
		case CLEAR:
			alertMessage = "RAM usage has returned to normal";
			break;
		default:
			alertMessage = "RAM usage has reached " + ramUsed + "% used";
			break;
		}
		alertSender.sendAlert(criticality, alertMessage);
	}
}
