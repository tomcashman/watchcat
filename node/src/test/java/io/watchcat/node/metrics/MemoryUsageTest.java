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
 * Unit tests for {@link MemoryUsage}
 *
 * @author Thomas Cashman
 */
public class MemoryUsageTest {
	private MemoryUsage memoryUsage;
	
	@Before
	public void setup() {
		memoryUsage = new MemoryUsage();
	}
	
	@Test
	public void testInitialValues() {
		Assert.assertEquals(0, memoryUsage.getTotalMemory());
		Assert.assertEquals(0, memoryUsage.getTotalSwap());
		Assert.assertEquals(0, memoryUsage.getUsedMemory());
		Assert.assertEquals(0, memoryUsage.getUsedSwap());
	}

	@Test
	public void testRun() {
		memoryUsage.run();
		Assert.assertEquals(true, memoryUsage.getTotalMemory() > 0);
		Assert.assertEquals(true, memoryUsage.getTotalSwap() > 0);
	}
	
	@Test
	public void testSettersGetters() {
		int testValue = 100;
		
		Assert.assertEquals(0, memoryUsage.getTotalMemory());
		memoryUsage.setTotalMemory(testValue);
		Assert.assertEquals(testValue, memoryUsage.getTotalMemory());
		
		Assert.assertEquals(0, memoryUsage.getTotalSwap());
		memoryUsage.setTotalSwap(testValue);
		Assert.assertEquals(testValue, memoryUsage.getTotalSwap());
		
		Assert.assertEquals(0, memoryUsage.getUsedMemory());
		memoryUsage.setUsedMemory(testValue);
		Assert.assertEquals(testValue, memoryUsage.getUsedMemory());
		
		Assert.assertEquals(0, memoryUsage.getUsedSwap());
		memoryUsage.setUsedSwap(testValue);
		Assert.assertEquals(testValue, memoryUsage.getUsedSwap());
	}

	@Test
	public void testToJson() throws IOException {
		long timestamp = System.currentTimeMillis();
		XContentBuilder content = memoryUsage.toJson(timestamp);
		String json = content.string();
		
		Assert.assertEquals(true, json.contains("\"timestamp\":" + timestamp));
		Assert.assertEquals(true, json.contains("\"totalMemory\":" + memoryUsage.getTotalMemory()));
		Assert.assertEquals(true, json.contains("\"totalSwap\":" + memoryUsage.getTotalSwap()));
		Assert.assertEquals(true, json.contains("\"usedMemory\":" + memoryUsage.getUsedMemory()));
		Assert.assertEquals(true, json.contains("\"usedSwap\":" + memoryUsage.getUsedSwap()));
	}

}
