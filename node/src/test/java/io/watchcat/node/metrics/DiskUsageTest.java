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
 * Unit tests for {@link DiskUsage}
 *
 * @author Thomas Cashman
 */
public class DiskUsageTest {
	private DiskUsage diskUsage;
	
	@Before
	public void setup() {
		diskUsage = new DiskUsage();
	}

	@Test
	public void testRun() {
		Assert.assertEquals(0, diskUsage.getDisks().size());
		diskUsage.run();
		Assert.assertEquals(true, diskUsage.getDisks().size() > 0);
	}

	@Test
	public void testToJsonWithNoData() throws IOException {
		long timestamp = System.currentTimeMillis();
		XContentBuilder content = diskUsage.toJson(timestamp);
		String json = content.string();
		
		Assert.assertEquals(true, json.contains("\"timestamp\":" + timestamp));
		Assert.assertEquals(true, json.contains("\"disks\":[]"));
	}

	@Test
	public void testToJsonWithData() throws IOException {
		diskUsage.run();
		
		long timestamp = System.currentTimeMillis();
		XContentBuilder content = diskUsage.toJson(timestamp);
		String json = content.string();
		
		Assert.assertEquals(true, json.contains("\"timestamp\":" + timestamp));
		Assert.assertEquals(true, json.contains("\"disks\":[{"));
		Assert.assertEquals(true, json.endsWith("}]}"));
	}
}
