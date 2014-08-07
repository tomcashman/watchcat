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
 * Unit tests for {@link Processes}
 *
 * @author Thomas Cashman
 */
public class ProcessesTest {
	private Processes processes;
	
	@Before
	public void setup() {
		processes = new Processes();
	}

	@Test
	public void testRun() {
		Assert.assertEquals(0, processes.getProcesses().size());
		processes.run();
		Assert.assertEquals(true, processes.getProcesses().size() > 0);
	}

	@Test
	public void testToJsonWithNoData() throws IOException {
		long timestamp = System.currentTimeMillis();
		XContentBuilder content = processes.toJson(timestamp);
		String json = content.string();
		
		Assert.assertEquals(true, json.contains("\"timestamp\":" + timestamp));
		Assert.assertEquals(true, json.contains("\"processes\":[]"));
	}

	@Test
	public void testToJsonWithData() throws IOException {
		processes.run();
		
		long timestamp = System.currentTimeMillis();
		XContentBuilder content = processes.toJson(timestamp);
		String json = content.string();
		
		Assert.assertEquals(true, json.contains("\"timestamp\":" + timestamp));
		Assert.assertEquals(true, json.contains("\"processes\":[{"));
		Assert.assertEquals(true, json.endsWith("}]}"));
	}
}
