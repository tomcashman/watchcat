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
package io.watchcat.node.metrics;

import java.io.IOException;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link LoadAverage}
 *
 * @author Thomas Cashman
 */
public class LoadAverageTest {
	private LoadAverage loadAverage;
	
	@Before
	public void setup() {
		loadAverage = new LoadAverage();
	}
	
	@Test
	public void testSettersGetters() {
		double testValue = 300.5;
		Assert.assertNotEquals(testValue, loadAverage.getOneMinuteAverage(), 0.1);
		loadAverage.setOneMinuteAverage(testValue);
		Assert.assertEquals(testValue, loadAverage.getOneMinuteAverage(), 0.1);
		
		Assert.assertNotEquals(testValue, loadAverage.getFiveMinuteAverage(), 0.1);
		loadAverage.setFiveMinuteAverage(testValue);
		Assert.assertEquals(testValue, loadAverage.getFiveMinuteAverage(), 0.1);
		
		Assert.assertNotEquals(testValue, loadAverage.getFifteenMinuteAverage(), 0.1);
		loadAverage.setFifteenMinuteAverage(testValue);
		Assert.assertEquals(testValue, loadAverage.getFifteenMinuteAverage(), 0.1);
	}

	@Test
	public void testToJson() throws IOException {
		long timestamp = System.currentTimeMillis();
		XContentBuilder content = loadAverage.toJson(timestamp);
		String json = content.string();
		
		Assert.assertEquals(true, json.contains("\"timestamp\":" + timestamp));
		Assert.assertEquals(true, json.contains("\"oneMinuteAverage\":" + loadAverage.getOneMinuteAverage()));
		Assert.assertEquals(true, json.contains("\"fiveMinuteAverage\":" + loadAverage.getFiveMinuteAverage()));
		Assert.assertEquals(true, json.contains("\"fifteenMinuteAverage\":" + loadAverage.getFifteenMinuteAverage()));
		Assert.assertEquals(true, json.contains("\"cpuCores\":" + loadAverage.getNumberOfCpuCores()));
	}
}
