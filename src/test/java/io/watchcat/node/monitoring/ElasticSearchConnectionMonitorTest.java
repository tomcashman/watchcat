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

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.collect.ImmutableList;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit and integration tests for {@link ElasticSearchConnectionMonitor}
 *
 * @author Thomas Cashman
 */
public class ElasticSearchConnectionMonitorTest {

	private ElasticSearchConnectionMonitor elasticSearchConnectionMonitor;
	private Mockery mockery;

	private ScheduledExecutorService scheduledExecutorService;
	private AlertSender alertSender;
	private TransportClient transportClient;
	private DiscoveryNode discoveryNode;
	
	private ImmutableList<DiscoveryNode> connectedNodes;

	@Before
	public void setup() {
		mockery = new Mockery();
		mockery.setImposteriser(ClassImposteriser.INSTANCE);

		scheduledExecutorService = mockery.mock(ScheduledExecutorService.class);
		alertSender = mockery.mock(AlertSender.class);
		transportClient = mockery.mock(TransportClient.class);
		discoveryNode = mockery.mock(DiscoveryNode.class);

		elasticSearchConnectionMonitor = new ElasticSearchConnectionMonitor();
		elasticSearchConnectionMonitor.setAlertSender(alertSender);
		elasticSearchConnectionMonitor
				.setScheduledExecutorService(scheduledExecutorService);
		elasticSearchConnectionMonitor.setTransportClient(transportClient);
	}

	@After
	public void teardown() {
		mockery.assertIsSatisfied();
	}

	@Test
	public void testPostConstruct() {
		mockery.checking(new Expectations() {
			{
				oneOf(scheduledExecutorService).schedule(
						with(elasticSearchConnectionMonitor),
						with(ElasticSearchConnectionMonitor.INTERVAL + 1L),
						with(TimeUnit.SECONDS));
			}
		});
		elasticSearchConnectionMonitor.postConstruct();
	}
}
