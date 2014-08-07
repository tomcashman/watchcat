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
import io.watchcat.node.event.NoElasticSearchConnectionEvent;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.elasticsearch.client.transport.TransportClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Monitors {@link TransportClient} to ensure there is a conneciton to
 * ElasticSearch and raises the appropriate event if not
 *
 * @author Thomas Cashman
 */
@Component
public class ElasticSearchConnectionMonitor implements Runnable {
	public static final int INTERVAL = 1;
	
	@Autowired
	private TransportClient transportClient;
	@Autowired
	private ScheduledExecutorService scheduledExecutorService;
	@Autowired
	private AlertSender alertSender;
	
	private NoElasticSearchConnectionEvent noElasticSearchConnectionEvent;
	
	@PostConstruct
	public void postConstruct() {
		scheduledExecutorService.schedule(this, INTERVAL + 1, TimeUnit.SECONDS);
	}
	
	@Override
	public void run() {
		if(noElasticSearchConnectionEvent != null) {
			if(transportClient.connectedNodes().size() > 0) {
				noElasticSearchConnectionEvent.end();
				noElasticSearchConnectionEvent = null;
			}
		} else if(transportClient.connectedNodes().size() == 0) {
			noElasticSearchConnectionEvent = new NoElasticSearchConnectionEvent(alertSender);
			noElasticSearchConnectionEvent.begin();
		}
		scheduledExecutorService.schedule(this, INTERVAL, TimeUnit.SECONDS);
	}

	public NoElasticSearchConnectionEvent getNoElasticSearchConnectionEvent() {
		return noElasticSearchConnectionEvent;
	}

	public void setNoElasticSearchConnectionEvent(
			NoElasticSearchConnectionEvent noElasticSearchConnectionEvent) {
		this.noElasticSearchConnectionEvent = noElasticSearchConnectionEvent;
	}

	public void setTransportClient(TransportClient transportClient) {
		this.transportClient = transportClient;
	}

	public void setScheduledExecutorService(
			ScheduledExecutorService scheduledExecutorService) {
		this.scheduledExecutorService = scheduledExecutorService;
	}

	public void setAlertSender(AlertSender alertSender) {
		this.alertSender = alertSender;
	}
}
