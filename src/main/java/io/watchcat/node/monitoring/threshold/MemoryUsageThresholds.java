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

import io.watchcat.node.metrics.MemoryUsage;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.springframework.stereotype.Component;

/**
 * Stores alert thresholds for {@link MemoryUsage}
 *
 * @author Thomas Cashman
 */
@Component
public class MemoryUsageThresholds {
	public static final int DEFAULT_MINOR_THRESHOLD = 80;
	public static final int DEFAULT_MAJOR_THRESHOLD = 90;
	public static final int DEFAULT_CRITICAL_THRESHOLD = 95;
	
	private AtomicInteger usedMemoryMinorThreshold, usedMemoryMajorThreshold, usedMemoryCriticalThreshold;
	private AtomicInteger usedSwapMinorThreshold, usedSwapMajorThreshold, usedSwapCriticalThreshold;
	
	public MemoryUsageThresholds() {
		usedMemoryMinorThreshold = new AtomicInteger(DEFAULT_MINOR_THRESHOLD);
		usedMemoryMajorThreshold = new AtomicInteger(DEFAULT_MAJOR_THRESHOLD);
		usedMemoryCriticalThreshold = new AtomicInteger(DEFAULT_CRITICAL_THRESHOLD);
		
		usedSwapMinorThreshold = new AtomicInteger(DEFAULT_MINOR_THRESHOLD);
		usedSwapMajorThreshold = new AtomicInteger(DEFAULT_MAJOR_THRESHOLD);
		usedSwapCriticalThreshold = new AtomicInteger(DEFAULT_CRITICAL_THRESHOLD);
	}
	
	public void fromGetResponse(GetResponse response) {
		Map<String, Object> values = response.getSourceAsMap();
		
		usedMemoryMinorThreshold.set(Integer.parseInt(values.get("usedMemoryMinorThreshold").toString()));
		usedMemoryMajorThreshold.set(Integer.parseInt(values.get("usedMemoryMajorThreshold").toString()));
		usedMemoryCriticalThreshold.set(Integer.parseInt(values.get("usedMemoryCriticalThreshold").toString()));
		
		usedSwapMinorThreshold.set(Integer.parseInt(values.get("usedSwapMinorThreshold").toString()));
		usedSwapMajorThreshold.set(Integer.parseInt(values.get("usedSwapMajorThreshold").toString()));
		usedSwapCriticalThreshold.set(Integer.parseInt(values.get("usedSwapCriticalThreshold").toString()));
	}
	
	public XContentBuilder toJson() {
		try {
			XContentBuilder builder = XContentFactory.jsonBuilder();
			builder = builder.startObject();
			builder = builder.field("usedMemoryMinorThreshold", getUsedMemoryMinorThreshold());
			builder = builder.field("usedMemoryMajorThreshold", getUsedMemoryMajorThreshold());
			builder = builder.field("usedMemoryCriticalThreshold", getUsedMemoryCriticalThreshold());
			
			builder = builder.field("usedSwapMinorThreshold", getUsedSwapMinorThreshold());
			builder = builder.field("usedSwapMajorThreshold", getUsedSwapMajorThreshold());
			builder = builder.field("usedSwapCriticalThreshold", getUsedSwapCriticalThreshold());
			builder = builder.endObject();
			return builder;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public int getUsedMemoryMinorThreshold() {
		return usedMemoryMinorThreshold.get();
	}

	public void setUsedMemoryMinorThreshold(int usedMemoryMinorThreshold) {
		this.usedMemoryMinorThreshold.set(usedMemoryMinorThreshold);
	}

	public int getUsedMemoryMajorThreshold() {
		return usedMemoryMajorThreshold.get();
	}

	public void setUsedMemoryMajorThreshold(int usedMemoryMajorThreshold) {
		this.usedMemoryMajorThreshold.set(usedMemoryMajorThreshold);
	}

	public int getUsedMemoryCriticalThreshold() {
		return usedMemoryCriticalThreshold.get();
	}

	public void setUsedMemoryCriticalThreshold(
			int usedMemoryCriticalThreshold) {
		this.usedMemoryCriticalThreshold.set(usedMemoryCriticalThreshold);
	}

	public int getUsedSwapMinorThreshold() {
		return usedSwapMinorThreshold.get();
	}

	public void setUsedSwapMinorThreshold(int usedSwapMinorThreshold) {
		this.usedSwapMinorThreshold.set(usedSwapMinorThreshold);
	}

	public int getUsedSwapMajorThreshold() {
		return usedSwapMajorThreshold.get();
	}

	public void setUsedSwapMajorThreshold(int usedSwapMajorThreshold) {
		this.usedSwapMajorThreshold.set(usedSwapMajorThreshold);
	}

	public int getUsedSwapCriticalThreshold() {
		return usedSwapCriticalThreshold.get();
	}

	public void setUsedSwapCriticalThreshold(int usedSwapCriticalThreshold) {
		this.usedSwapCriticalThreshold.set(usedSwapCriticalThreshold);
	}
}
