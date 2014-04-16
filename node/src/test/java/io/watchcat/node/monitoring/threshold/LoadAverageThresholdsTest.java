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
package io.watchcat.node.monitoring.threshold;

import io.watchcat.node.metrics.LoadAverage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link LoadAverageThresholds}
 *
 * @author Thomas Cashman
 */
public class LoadAverageThresholdsTest {
	private LoadAverageThresholds loadAverageThresholds;
	private Mockery mockery;
	
	private LoadAverage loadAverage;
	
	@Before
	public void setup() {
		mockery = new Mockery();
		mockery.setImposteriser(ClassImposteriser.INSTANCE);
		
		loadAverageThresholds = new LoadAverageThresholds();
		
		loadAverage = mockery.mock(LoadAverage.class);
		loadAverageThresholds.setLoadAverage(loadAverage);
	}
	
	@After
	public void teardown() {
		mockery.assertIsSatisfied();
	}

	@Test
	public void testPostConstruct() {
		mockery.checking(new Expectations() {
			{
				oneOf(loadAverage).getNumberOfCpuCores();
				will(returnValue(1));
			}
		});
		loadAverageThresholds.postConstruct();
		
		Assert.assertEquals(true, loadAverageThresholds.getOneMinuteAverageMinorThreshold() > 0);
		Assert.assertEquals(true, loadAverageThresholds.getOneMinuteAverageMajorThreshold() > loadAverageThresholds.getOneMinuteAverageMinorThreshold());
		Assert.assertEquals(true, loadAverageThresholds.getOneMinuteAverageCriticalThreshold() > loadAverageThresholds.getOneMinuteAverageMajorThreshold());
		
		Assert.assertEquals(true, loadAverageThresholds.getFiveMinuteAverageMinorThreshold() > 0);
		Assert.assertEquals(true, loadAverageThresholds.getFiveMinuteAverageMajorThreshold() > loadAverageThresholds.getFiveMinuteAverageMinorThreshold());
		Assert.assertEquals(true, loadAverageThresholds.getFiveMinuteAverageCriticalThreshold() > loadAverageThresholds.getFiveMinuteAverageMajorThreshold());
		
		Assert.assertEquals(true, loadAverageThresholds.getFifteenMinuteAverageMinorThreshold() > 0);
		Assert.assertEquals(true, loadAverageThresholds.getFifteenMinuteAverageMajorThreshold() > loadAverageThresholds.getFifteenMinuteAverageMinorThreshold());
		Assert.assertEquals(true, loadAverageThresholds.getFifteenMinuteAverageCriticalThreshold() > loadAverageThresholds.getFifteenMinuteAverageMajorThreshold());
	}

	@Test
	public void testFromGetResponse() {
		loadAverageThresholds = new LoadAverageThresholds(1.0);
		
		double minorThreshold = 1;
		double majorThreshold = 2;
		double criticalThreshold = 3;
		
		final Map<String, Object> values = new HashMap<String, Object>();
		values.put("oneMinuteAverageMinorThreshold", minorThreshold);
		values.put("oneMinuteAverageMajorThreshold", majorThreshold);
		values.put("oneMinuteAverageCriticalThreshold", criticalThreshold);
		values.put("fiveMinuteAverageMinorThreshold", minorThreshold + 1);
		values.put("fiveMinuteAverageMajorThreshold", majorThreshold + 1);
		values.put("fiveMinuteAverageCriticalThreshold", criticalThreshold + 1);
		values.put("fifteenMinuteAverageMinorThreshold", minorThreshold + 2);
		values.put("fifteenMinuteAverageMajorThreshold", majorThreshold + 2);
		values.put("fifteenMinuteAverageCriticalThreshold", criticalThreshold + 2);
		final GetResponse getResponse = mockery.mock(GetResponse.class);
		
		mockery.checking(new Expectations() {
			{
				oneOf(getResponse).getSourceAsMap();
				will(returnValue(values));
			}
		});
		loadAverageThresholds.fromGetResponse(getResponse);
		
		Assert.assertEquals(minorThreshold, loadAverageThresholds.getOneMinuteAverageMinorThreshold(), 0.1);
		Assert.assertEquals(majorThreshold, loadAverageThresholds.getOneMinuteAverageMajorThreshold(), 0.1);
		Assert.assertEquals(criticalThreshold, loadAverageThresholds.getOneMinuteAverageCriticalThreshold(), 0.1);
		
		Assert.assertEquals(minorThreshold + 1, loadAverageThresholds.getFiveMinuteAverageMinorThreshold(), 0.1);
		Assert.assertEquals(majorThreshold + 1, loadAverageThresholds.getFiveMinuteAverageMajorThreshold(), 0.1);
		Assert.assertEquals(criticalThreshold + 1, loadAverageThresholds.getFiveMinuteAverageCriticalThreshold(), 0.1);
		
		Assert.assertEquals(minorThreshold + 2, loadAverageThresholds.getFifteenMinuteAverageMinorThreshold(), 0.1);
		Assert.assertEquals(majorThreshold + 2, loadAverageThresholds.getFifteenMinuteAverageMajorThreshold(), 0.1);
		Assert.assertEquals(criticalThreshold + 2, loadAverageThresholds.getFifteenMinuteAverageCriticalThreshold(), 0.1);
	}

	@Test
	public void testToJson() throws IOException {
		loadAverageThresholds = new LoadAverageThresholds(1.0);
		XContentBuilder contentBuilder = loadAverageThresholds.toJson();
		String json = contentBuilder.string();
		
		Assert.assertEquals(true, json.contains("\"oneMinuteAverageMinorThreshold\":"));
		Assert.assertEquals(true, json.contains("\"oneMinuteAverageMajorThreshold\":"));
		Assert.assertEquals(true, json.contains("\"oneMinuteAverageCriticalThreshold\":"));
		
		Assert.assertEquals(true, json.contains("\"fiveMinuteAverageMinorThreshold\":"));
		Assert.assertEquals(true, json.contains("\"fiveMinuteAverageMajorThreshold\":"));
		Assert.assertEquals(true, json.contains("\"fiveMinuteAverageCriticalThreshold\":"));
		
		Assert.assertEquals(true, json.contains("\"fifteenMinuteAverageMinorThreshold\":"));
		Assert.assertEquals(true, json.contains("\"fifteenMinuteAverageMajorThreshold\":"));
		Assert.assertEquals(true, json.contains("\"fifteenMinuteAverageCriticalThreshold\":"));
	}

}
