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
package com.viridiansoftware.watchcat.node.monitoring.threshold;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.springframework.stereotype.Component;

import com.viridiansoftware.watchcat.node.metrics.domain.Filesystem;

/**
 * Stores alert thresholds for {@link Filesystem}s
 *
 * @author Thomas Cashman
 */
@Component
public class FilesystemThresholds {
	private AtomicInteger minorThreshold;
	private AtomicInteger majorThreshold;
	private AtomicInteger criticalThreshold;
	
	public FilesystemThresholds() {
		minorThreshold = new AtomicInteger(80);
		majorThreshold = new AtomicInteger(90);
		criticalThreshold = new AtomicInteger(95);
	}
	
	public XContentBuilder toJson() {
		try {
			XContentBuilder builder = XContentFactory.jsonBuilder();
			builder = builder.startObject();
			builder = builder.field("minorThreshold", getMinorThreshold());
			builder = builder.field("majorThreshold", getMajorThreshold());
			builder = builder.field("criticalThreshold", getCriticalThreshold());
			builder = builder.endObject();
			return builder;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void fromJson(GetResponse response) {
		Map<String, Object> values = response.getSourceAsMap();
		
		minorThreshold.set(Integer.parseInt(values.get("minorThreshold").toString()));
		majorThreshold.set(Integer.parseInt(values.get("majorThreshold").toString()));
		criticalThreshold.set(Integer.parseInt(values.get("criticalThreshold").toString()));
	}

	public int getMinorThreshold() {
		return minorThreshold.get();
	}

	public void setMinorThreshold(int minorThreshold) {
		this.minorThreshold.set(minorThreshold);
	}

	public int getMajorThreshold() {
		return majorThreshold.get();
	}

	public void setMajorThreshold(int majorThreshold) {
		this.majorThreshold.set(majorThreshold);
	}

	public int getCriticalThreshold() {
		return criticalThreshold.get();
	}

	public void setCriticalThreshold(int criticalThreshold) {
		this.criticalThreshold.set(criticalThreshold);
	}
}
