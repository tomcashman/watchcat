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
 * An event raised when there is no connection to ElasticSearch
 *
 * @author Thomas Cashman
 */
public class NoElasticSearchConnectionEvent implements StatusEvent {
	private AlertSender alertSender;
	private Criticality criticality;

	public NoElasticSearchConnectionEvent(AlertSender alertSender) {
		this.alertSender = alertSender;
	}
	
	@Override
	public void begin(String... eventParams) {
		this.criticality = Criticality.CRITICAL;
		alert();
	}

	@Override
	public void end(String... eventParams) {
		this.criticality = Criticality.CLEAR;
		alert();
	}

	@Override
	public void alert() {
		String alertMessage;
		switch (criticality) {
		case CLEAR:
			alertMessage = "Connection to ElasticSearch has been restored";
			break;
		default:
			alertMessage = "No connection to ElasticSearch. All metrics will be lost.";
			break;
		}
		alertSender.sendAlert(criticality, alertMessage);
	}
}
