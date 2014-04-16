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
 * Unit tests for {@link DiskThresholds}
 *
 * @author Thomas Cashman
 */
public class DiskThresholdsTest {
	private DiskThresholds diskThresholds;
	private Mockery mockery;

	@Before
	public void setup() {
		mockery = new Mockery();
		mockery.setImposteriser(ClassImposteriser.INSTANCE);
		
		diskThresholds = new DiskThresholds();
	}
	
	@After
	public void teardown() {
		mockery.assertIsSatisfied();
	}

	@Test
	public void testInitialValues() {
		Assert.assertEquals(DiskThresholds.DEFAULT_MINOR_THRESHOLD, diskThresholds.getMinorThreshold());
		Assert.assertEquals(DiskThresholds.DEFAULT_MAJOR_THRESHOLD, diskThresholds.getMajorThreshold());
		Assert.assertEquals(DiskThresholds.DEFAULT_CRITICAL_THRESHOLD, diskThresholds.getCriticalThreshold());
	}

	@Test
	public void testToJson() throws IOException {
		XContentBuilder contentBuilder = diskThresholds.toJson();
		String json = contentBuilder.string();
		Assert.assertEquals(true, json.contains("\"minorThreshold\":" + DiskThresholds.DEFAULT_MINOR_THRESHOLD));
		Assert.assertEquals(true, json.contains("\"majorThreshold\":" + DiskThresholds.DEFAULT_MAJOR_THRESHOLD));
		Assert.assertEquals(true, json.contains("\"criticalThreshold\":" + DiskThresholds.DEFAULT_CRITICAL_THRESHOLD));
	}

	@Test
	public void testFromGetResponse() {
		final Map<String, Object> values = new HashMap<String, Object>();
		values.put("minorThreshold", DiskThresholds.DEFAULT_MINOR_THRESHOLD + 1);
		values.put("majorThreshold", DiskThresholds.DEFAULT_MAJOR_THRESHOLD + 1);
		values.put("criticalThreshold", DiskThresholds.DEFAULT_CRITICAL_THRESHOLD + 1);
		final GetResponse getResponse = mockery.mock(GetResponse.class);
		
		mockery.checking(new Expectations() {
			{
				oneOf(getResponse).getSourceAsMap();
				will(returnValue(values));
			}
		});
		
		diskThresholds.fromGetResponse(getResponse);
		
		Assert.assertEquals(DiskThresholds.DEFAULT_MINOR_THRESHOLD + 1, diskThresholds.getMinorThreshold());
		Assert.assertEquals(DiskThresholds.DEFAULT_MAJOR_THRESHOLD + 1, diskThresholds.getMajorThreshold());
		Assert.assertEquals(DiskThresholds.DEFAULT_CRITICAL_THRESHOLD + 1, diskThresholds.getCriticalThreshold());
	}

}
