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
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link EmailAddressPoller}
 *
 * @author Thomas Cashman
 */
public class EmailAddressPollerTest {
	private EmailAddressPoller emailAddressPoller;
	private Mockery mockery;

	private ScheduledExecutorService scheduledExecutorService;
	private TransportClient transportClient;
	private GetRequestBuilder getRequestBuilder;
	private ListenableActionFuture<GetResponse> actionFuture;
	private GetResponse getResponse;
	private String hostname = "host";
	
	@Before
	public void setup() {
		mockery = new Mockery();
		mockery.setImposteriser(ClassImposteriser.INSTANCE);

		scheduledExecutorService = mockery.mock(ScheduledExecutorService.class);
		transportClient = mockery.mock(TransportClient.class);
		getRequestBuilder = mockery.mock(GetRequestBuilder.class);
		actionFuture = mockery.mock(ListenableActionFuture.class);
		getResponse = mockery.mock(GetResponse.class);

		emailAddressPoller = new EmailAddressPoller();
		emailAddressPoller.setHostname(hostname);
		emailAddressPoller.setScheduledExecutorService(scheduledExecutorService);
		emailAddressPoller.setTransportClient(transportClient);
	}

	@Test
	public void testPostConstruct() {
		mockery.checking(new Expectations() {
			{
				oneOf(scheduledExecutorService).scheduleAtFixedRate(
						emailAddressPoller, EmailAddressPoller.INITIAL_DELAY,
						EmailAddressPoller.INTERVAL, TimeUnit.SECONDS);
			}
		});
		emailAddressPoller.postConstruct();
		Assert.assertEquals(0, emailAddressPoller.getEmailAddresses().size());
	}

	@Test
	public void testRunWithNoData() {
		Assert.assertEquals(0, emailAddressPoller.getEmailAddresses().size());
		
		mockery.checking(new Expectations() {
			{
				oneOf(transportClient).prepareGet(hostname, ElasticSearchConstants.ALERT_DESTINATION_TYPE,
						ElasticSearchConstants.EMAIL_ADDRESSES);
				will(returnValue(getRequestBuilder));
				oneOf(getRequestBuilder).execute();
				will(returnValue(actionFuture));
				oneOf(actionFuture).actionGet();
				will(returnValue(getResponse));
				oneOf(getResponse).isExists();
				will(returnValue(false));
			}
		});
		emailAddressPoller.run();
		
		Assert.assertEquals(0, emailAddressPoller.getEmailAddresses().size());
	}

	@Test
	public void testRunWithData() {
		Assert.assertEquals(0, emailAddressPoller.getEmailAddresses().size());
		
		final List<String> emailAddresses = new ArrayList<String>();
		emailAddresses.add("test@test.com");
		
		mockery.checking(new Expectations() {
			{
				oneOf(transportClient).prepareGet(hostname, ElasticSearchConstants.ALERT_DESTINATION_TYPE,
						ElasticSearchConstants.EMAIL_ADDRESSES);
				will(returnValue(getRequestBuilder));
				oneOf(getRequestBuilder).execute();
				will(returnValue(actionFuture));
				oneOf(actionFuture).actionGet();
				will(returnValue(getResponse));
				oneOf(getResponse).isExists();
				will(returnValue(true));
				
				oneOf(getResponse).getSourceAsMap();
				will(returnValue(new HashMap<String, Object>() {
					{
						put("list", emailAddresses);
					}
				}));
			}
		});
		emailAddressPoller.run();
		
		Assert.assertEquals(emailAddresses.size(), emailAddressPoller.getEmailAddresses().size());
	}
}
