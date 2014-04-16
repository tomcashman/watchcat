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
 * Unit tests for {@link MemoryUsageThresholds}
 *
 * @author Thomas Cashman
 */
public class MemoryUsageThresholdsTest {
	private MemoryUsageThresholds memoryUsageThresholds;
	private Mockery mockery;

	@Before
	public void setup() {
		mockery = new Mockery();
		mockery.setImposteriser(ClassImposteriser.INSTANCE);

		memoryUsageThresholds = new MemoryUsageThresholds();
	}

	@After
	public void teardown() {
		mockery.assertIsSatisfied();
	}

	@Test
	public void testInitialValues() {
		Assert.assertEquals(MemoryUsageThresholds.DEFAULT_MINOR_THRESHOLD,
				memoryUsageThresholds.getUsedMemoryMinorThreshold());
		Assert.assertEquals(MemoryUsageThresholds.DEFAULT_MAJOR_THRESHOLD,
				memoryUsageThresholds.getUsedMemoryMajorThreshold());
		Assert.assertEquals(MemoryUsageThresholds.DEFAULT_CRITICAL_THRESHOLD,
				memoryUsageThresholds.getUsedMemoryCriticalThreshold());
		
		Assert.assertEquals(MemoryUsageThresholds.DEFAULT_MINOR_THRESHOLD,
				memoryUsageThresholds.getUsedSwapMinorThreshold());
		Assert.assertEquals(MemoryUsageThresholds.DEFAULT_MAJOR_THRESHOLD,
				memoryUsageThresholds.getUsedSwapMajorThreshold());
		Assert.assertEquals(MemoryUsageThresholds.DEFAULT_CRITICAL_THRESHOLD,
				memoryUsageThresholds.getUsedSwapCriticalThreshold());
	}

	@Test
	public void testFromGetResponse() {
		final Map<String, Object> values = new HashMap<String, Object>();
		values.put("usedMemoryMinorThreshold", MemoryUsageThresholds.DEFAULT_MINOR_THRESHOLD + 1);
		values.put("usedMemoryMajorThreshold", MemoryUsageThresholds.DEFAULT_MAJOR_THRESHOLD + 1);
		values.put("usedMemoryCriticalThreshold", MemoryUsageThresholds.DEFAULT_CRITICAL_THRESHOLD + 1);
		values.put("usedSwapMinorThreshold", MemoryUsageThresholds.DEFAULT_MINOR_THRESHOLD + 1);
		values.put("usedSwapMajorThreshold", MemoryUsageThresholds.DEFAULT_MAJOR_THRESHOLD + 1);
		values.put("usedSwapCriticalThreshold", MemoryUsageThresholds.DEFAULT_CRITICAL_THRESHOLD + 1);
		final GetResponse getResponse = mockery.mock(GetResponse.class);
		
		mockery.checking(new Expectations() {
			{
				oneOf(getResponse).getSourceAsMap();
				will(returnValue(values));
			}
		});
		
		memoryUsageThresholds.fromGetResponse(getResponse);
		Assert.assertEquals(MemoryUsageThresholds.DEFAULT_MINOR_THRESHOLD + 1, memoryUsageThresholds.getUsedMemoryMinorThreshold());
		Assert.assertEquals(MemoryUsageThresholds.DEFAULT_MAJOR_THRESHOLD + 1, memoryUsageThresholds.getUsedMemoryMajorThreshold());
		Assert.assertEquals(MemoryUsageThresholds.DEFAULT_CRITICAL_THRESHOLD + 1, memoryUsageThresholds.getUsedMemoryCriticalThreshold());

		Assert.assertEquals(MemoryUsageThresholds.DEFAULT_MINOR_THRESHOLD + 1, memoryUsageThresholds.getUsedSwapMinorThreshold());
		Assert.assertEquals(MemoryUsageThresholds.DEFAULT_MAJOR_THRESHOLD + 1, memoryUsageThresholds.getUsedSwapMajorThreshold());
		Assert.assertEquals(MemoryUsageThresholds.DEFAULT_CRITICAL_THRESHOLD + 1, memoryUsageThresholds.getUsedSwapCriticalThreshold());
	}

	@Test
	public void testToJson() throws IOException {
		XContentBuilder contentBuilder = memoryUsageThresholds.toJson();
		String json = contentBuilder.string();
		
		Assert.assertEquals(true, json.contains("\"usedMemoryMinorThreshold\":" + MemoryUsageThresholds.DEFAULT_MINOR_THRESHOLD));
		Assert.assertEquals(true, json.contains("\"usedMemoryMajorThreshold\":" + MemoryUsageThresholds.DEFAULT_MAJOR_THRESHOLD));
		Assert.assertEquals(true, json.contains("\"usedMemoryCriticalThreshold\":" + MemoryUsageThresholds.DEFAULT_CRITICAL_THRESHOLD));

		Assert.assertEquals(true, json.contains("\"usedSwapMinorThreshold\":" + MemoryUsageThresholds.DEFAULT_MINOR_THRESHOLD));
		Assert.assertEquals(true, json.contains("\"usedSwapMajorThreshold\":" + MemoryUsageThresholds.DEFAULT_MAJOR_THRESHOLD));
		Assert.assertEquals(true, json.contains("\"usedSwapCriticalThreshold\":" + MemoryUsageThresholds.DEFAULT_CRITICAL_THRESHOLD));
	}

}
