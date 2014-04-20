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
package io.watchcat.node.alerts.email;

import io.watchcat.node.ElasticSearchConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.PostConstruct;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Polls list of email addresses that have been added to receive alerts
 *
 * @author Thomas Cashman
 */
@Component
public class EmailAddressPoller implements Runnable {
	public static final long INTERVAL = 10L;
	public static final long INITIAL_DELAY = 5L;
	
	@Autowired
	private TransportClient transportClient;
	@Autowired
	private ScheduledExecutorService scheduledExecutorService;
	@Autowired
	@Qualifier("hostname")
	private String hostname;
	
	private AtomicReference<List<String>> emailAddresses;
	
	public EmailAddressPoller() {
		emailAddresses = new AtomicReference<List<String>>(new ArrayList<String>(5));
	}
	
	@PostConstruct
	public void postConstruct() {
		scheduledExecutorService.scheduleAtFixedRate(this, INITIAL_DELAY, INTERVAL, TimeUnit.SECONDS);
	}
	
	@Override
	public void run() {
		GetResponse response = transportClient
				.prepareGet(hostname, ElasticSearchConstants.ALERT_DESTINATION_TYPE,
						ElasticSearchConstants.EMAIL_ADDRESSES).execute()
				.actionGet();
		if(!response.isExists()) {
			return;
		}
		
		Map<String, Object> fields = response.getSourceAsMap();
		List<String> emails = (List<String>) fields.get("list");
		emailAddresses.set(emails);
	}

	public List<String> getEmailAddresses() {
		return emailAddresses.get();
	}

	public void setTransportClient(TransportClient transportClient) {
		this.transportClient = transportClient;
	}

	public void setScheduledExecutorService(
			ScheduledExecutorService scheduledExecutorService) {
		this.scheduledExecutorService = scheduledExecutorService;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
}
