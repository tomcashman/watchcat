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
package io.watchcat.node.alerts;

import io.watchcat.node.ElasticSearchConstants;
import io.watchcat.node.alerts.email.EmailAddressPoller;
import io.watchcat.node.event.Criticality;

import java.util.ArrayList;
import java.util.Properties;

import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit and integration tests for {@link AlertSender}
 *
 * @author Thomas Cashman
 */
public class AlertSenderTest {

	private AlertSender alertSender;
	private Mockery mockery;
	
	private TransportClient transportClient;
	private EmailAddressPoller emailAddressPoller;
	private IndexRequestBuilder indexRequestBuilder;
	private ListenableActionFuture<IndexResponse> actionFuture;
	
	@Before
	public void setup() {
		mockery = new Mockery();
		mockery.setImposteriser(ClassImposteriser.INSTANCE);
		
		transportClient = mockery.mock(TransportClient.class);
		emailAddressPoller = mockery.mock(EmailAddressPoller.class);
		indexRequestBuilder = mockery.mock(IndexRequestBuilder.class);
		actionFuture = mockery.mock(ListenableActionFuture.class);
		
		alertSender = new AlertSender();
		alertSender.setTransportClient(transportClient);
		alertSender.setEmailAddressPoller(emailAddressPoller);
	}
	
	@Test
	public void testPostConstruct() {
		String smtpHost = "mail.google.com";
		String smtpPort = "803";
		String tls = "true";
		
		alertSender.setSmtpHost(smtpHost);
		alertSender.setSmtpPort(smtpPort);
		alertSender.setSmtpTls(tls);
		alertSender.postConstruct();
		
		Properties smtpProperties = alertSender.getSmtpHostProperties();
		Assert.assertEquals(smtpHost, smtpProperties.getProperty("mail.smtp.host"));
		Assert.assertEquals(smtpPort, smtpProperties.getProperty("mail.smtp.port"));
		Assert.assertEquals(tls, smtpProperties.getProperty("mail.smtp.starttls.enable"));
		Assert.assertEquals("true", smtpProperties.getProperty("mail.smtp.auth"));
	}

	@Test
	public void testSendAlertWithLogOnly() {
		String hostname = "host";
		alertSender.setHostname(hostname);
		
		String message = "This is an alert";
		
		mockery.checking(new Expectations() {
			{
				oneOf(transportClient).prepareIndex(with(ElasticSearchConstants.WATCHCAT_ALERTS_INDEX),
					with(ElasticSearchConstants.LOAD_AVERAGE), with(any(String.class)));
				will(returnValue(indexRequestBuilder));
				oneOf(indexRequestBuilder).setSource(with(any(XContentBuilder.class)));
				will(returnValue(indexRequestBuilder));
				oneOf(indexRequestBuilder).execute();
				will(returnValue(actionFuture));
				oneOf(actionFuture).actionGet();
				will(returnValue(new IndexResponse()));
				
				oneOf(emailAddressPoller).getEmailAddresses();
				will(returnValue(new ArrayList<String>()));
			}
		});
		
		alertSender.sendAlert(Criticality.CRITICAL, message);
	}

}
