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

/**
 *
 *
 * @author Thomas Cashman
 */
public class SwapUsageEvent implements CriticalityEvent {
	private AlertSender alertSender;
	private Criticality criticality;
	private String swapUsed;
	
	public SwapUsageEvent(AlertSender alertSender) {
		this.alertSender = alertSender;
	}
	
	public SwapUsageEvent(AlertSender alertSender, Criticality criticality) {
		this.alertSender = alertSender;
		this.criticality = criticality;
	}

	@Override
	public void begin(Criticality criticality, String... eventParams) {
		this.criticality = criticality;
		this.swapUsed = eventParams[0];
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
			this.swapUsed = eventParams[0];
			alert();
		}
	}

	@Override
	public void alert() {
		String alertMessage;
		switch (criticality) {
		case CLEAR:
			alertMessage = "Swap usage has returned to normal";
			break;
		default:
			alertMessage = "Swap usage has reached " + swapUsed + "% used";
			break;
		}
		alertSender.sendAlert(criticality, alertMessage);
	}
}
