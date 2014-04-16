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

import java.util.Map;

import javax.annotation.PostConstruct;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.common.util.concurrent.jsr166e.extra.AtomicDouble;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Stores alert thresholds for {@link LoadAverage}
 *
 * @author Thomas Cashman
 */
@Component
public class LoadAverageThresholds {
	private AtomicDouble oneMinuteAverageMinorThreshold, oneMinuteAverageMajorThreshold, oneMinuteAverageCriticalThreshold;
	private AtomicDouble fiveMinuteAverageMinorThreshold, fiveMinuteAverageMajorThreshold, fiveMinuteAverageCriticalThreshold;
	private AtomicDouble fifteenMinuteAverageMinorThreshold, fifteenMinuteAverageMajorThreshold, fifteenMinuteAverageCriticalThreshold;

	@Autowired
	private LoadAverage loadAverage;
	
	public LoadAverageThresholds() {}
	
	public LoadAverageThresholds(double initialMinorValue) {
		oneMinuteAverageCriticalThreshold = new AtomicDouble(initialMinorValue + 0.2d);
		oneMinuteAverageMajorThreshold = new AtomicDouble(initialMinorValue + 0.1d);
		oneMinuteAverageMinorThreshold = new AtomicDouble(initialMinorValue);
		
		fiveMinuteAverageCriticalThreshold = new AtomicDouble(initialMinorValue + 0.2d);
		fiveMinuteAverageMajorThreshold = new AtomicDouble(initialMinorValue + 0.1d);
		fiveMinuteAverageMinorThreshold = new AtomicDouble(initialMinorValue);
		
		fifteenMinuteAverageCriticalThreshold = new AtomicDouble(initialMinorValue + 0.2d);
		fifteenMinuteAverageMajorThreshold = new AtomicDouble(initialMinorValue + 0.1d);
		fifteenMinuteAverageMinorThreshold = new AtomicDouble(initialMinorValue);		
	}
	
	@PostConstruct
	public void postConstruct() {
		int numberOfCores = loadAverage.getNumberOfCpuCores();
		oneMinuteAverageCriticalThreshold = new AtomicDouble(numberOfCores + 3);
		oneMinuteAverageMajorThreshold = new AtomicDouble(numberOfCores + 2);
		oneMinuteAverageMinorThreshold = new AtomicDouble(numberOfCores + 1);
		
		fiveMinuteAverageCriticalThreshold = new AtomicDouble(numberOfCores + 2);
		fiveMinuteAverageMajorThreshold = new AtomicDouble(numberOfCores + 1);
		fiveMinuteAverageMinorThreshold = new AtomicDouble(numberOfCores);
		
		fifteenMinuteAverageCriticalThreshold = new AtomicDouble(numberOfCores + 0.25);
		fifteenMinuteAverageMajorThreshold = new AtomicDouble(numberOfCores);
		fifteenMinuteAverageMinorThreshold = new AtomicDouble(numberOfCores - 0.25);
	}
	
	public void fromGetResponse(GetResponse response) {
		Map<String, Object> values = response.getSourceAsMap();
		
		oneMinuteAverageMinorThreshold.set(Double.parseDouble(values.get("oneMinuteAverageMinorThreshold").toString()));
		oneMinuteAverageMajorThreshold.set(Double.parseDouble(values.get("oneMinuteAverageMajorThreshold").toString()));
		oneMinuteAverageCriticalThreshold.set(Double.parseDouble(values.get("oneMinuteAverageCriticalThreshold").toString()));

		fiveMinuteAverageMinorThreshold.set(Double.parseDouble(values.get("fiveMinuteAverageMinorThreshold").toString()));
		fiveMinuteAverageMajorThreshold.set(Double.parseDouble(values.get("fiveMinuteAverageMajorThreshold").toString()));
		fiveMinuteAverageCriticalThreshold.set(Double.parseDouble(values.get("fiveMinuteAverageCriticalThreshold").toString()));

		fifteenMinuteAverageMinorThreshold.set(Double.parseDouble(values.get("fifteenMinuteAverageMinorThreshold").toString()));
		fifteenMinuteAverageMajorThreshold.set(Double.parseDouble(values.get("fifteenMinuteAverageMajorThreshold").toString()));
		fifteenMinuteAverageCriticalThreshold.set(Double.parseDouble(values.get("fifteenMinuteAverageCriticalThreshold").toString()));
	}
	
	public XContentBuilder toJson() {
		try {
			XContentBuilder builder = XContentFactory.jsonBuilder();
			builder = builder.startObject();
			builder = builder.field("oneMinuteAverageMinorThreshold", getOneMinuteAverageMinorThreshold());
			builder = builder.field("oneMinuteAverageMajorThreshold", getOneMinuteAverageMajorThreshold());
			builder = builder.field("oneMinuteAverageCriticalThreshold", getOneMinuteAverageCriticalThreshold());
			
			builder = builder.field("fiveMinuteAverageMinorThreshold", getFiveMinuteAverageMinorThreshold());
			builder = builder.field("fiveMinuteAverageMajorThreshold", getFiveMinuteAverageMajorThreshold());
			builder = builder.field("fiveMinuteAverageCriticalThreshold", getFiveMinuteAverageCriticalThreshold());
			
			builder = builder.field("fifteenMinuteAverageMinorThreshold", getFifteenMinuteAverageMinorThreshold());
			builder = builder.field("fifteenMinuteAverageMajorThreshold", getFifteenMinuteAverageMajorThreshold());
			builder = builder.field("fifteenMinuteAverageCriticalThreshold", getFifteenMinuteAverageCriticalThreshold());
			builder = builder.endObject();
			return builder;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public double getOneMinuteAverageMinorThreshold() {
		return oneMinuteAverageMinorThreshold.get();
	}

	public void setOneMinuteAverageMinorThreshold(
			double oneMinuteAverageMinorThreshold) {
		this.oneMinuteAverageMinorThreshold.set(oneMinuteAverageMinorThreshold);
	}

	public double getOneMinuteAverageMajorThreshold() {
		return oneMinuteAverageMajorThreshold.get();
	}

	public void setOneMinuteAverageMajorThreshold(
			double oneMinuteAverageMajorThreshold) {
		this.oneMinuteAverageMajorThreshold.set(oneMinuteAverageMajorThreshold);
	}

	public double getOneMinuteAverageCriticalThreshold() {
		return oneMinuteAverageCriticalThreshold.get();
	}

	public void setOneMinuteAverageCriticalThreshold(
			double oneMinuteAverageCriticalThreshold) {
		this.oneMinuteAverageCriticalThreshold.set(oneMinuteAverageCriticalThreshold);
	}

	public double getFiveMinuteAverageMinorThreshold() {
		return fiveMinuteAverageMinorThreshold.get();
	}

	public void setFiveMinuteAverageMinorThreshold(
			double fiveMinuteAverageMinorThreshold) {
		this.fiveMinuteAverageMinorThreshold.set(fiveMinuteAverageMinorThreshold);
	}

	public double getFiveMinuteAverageMajorThreshold() {
		return fiveMinuteAverageMajorThreshold.get();
	}

	public void setFiveMinuteAverageMajorThreshold(
			double fiveMinuteAverageMajorThreshold) {
		this.fiveMinuteAverageMajorThreshold.set(fiveMinuteAverageMajorThreshold);
	}

	public double getFiveMinuteAverageCriticalThreshold() {
		return fiveMinuteAverageCriticalThreshold.get();
	}

	public void setFiveMinuteAverageCriticalThreshold(
			double fiveMinuteAverageCriticalThreshold) {
		this.fiveMinuteAverageCriticalThreshold.set(fiveMinuteAverageCriticalThreshold);
	}

	public double getFifteenMinuteAverageMinorThreshold() {
		return fifteenMinuteAverageMinorThreshold.get();
	}

	public void setFifteenMinuteAverageMinorThreshold(
			double fifteenMinuteAverageMinorThreshold) {
		this.fifteenMinuteAverageMinorThreshold.set(fifteenMinuteAverageMinorThreshold);
	}

	public double getFifteenMinuteAverageMajorThreshold() {
		return fifteenMinuteAverageMajorThreshold.get();
	}

	public void setFifteenMinuteAverageMajorThreshold(
			double fifteenMinuteAverageMajorThreshold) {
		this.fifteenMinuteAverageMajorThreshold.set(fifteenMinuteAverageMajorThreshold);
	}

	public double getFifteenMinuteAverageCriticalThreshold() {
		return fifteenMinuteAverageCriticalThreshold.get();
	}

	public void setFifteenMinuteAverageCriticalThreshold(
			double fifteenMinuteAverageCriticalThreshold) {
		this.fifteenMinuteAverageCriticalThreshold.set(fifteenMinuteAverageCriticalThreshold);
	}

	public void setLoadAverage(LoadAverage loadAverage) {
		this.loadAverage = loadAverage;
	}
}
