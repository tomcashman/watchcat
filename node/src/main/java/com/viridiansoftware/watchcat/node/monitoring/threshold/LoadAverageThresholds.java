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

import org.elasticsearch.common.util.concurrent.jsr166e.extra.AtomicDouble;
import org.springframework.stereotype.Component;

import com.viridiansoftware.watchcat.node.metrics.LoadAverage;

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

	public LoadAverageThresholds() {
		oneMinuteAverageCriticalThreshold = new AtomicDouble(Double.MAX_VALUE);
		oneMinuteAverageMajorThreshold = new AtomicDouble(Double.MAX_VALUE);
		oneMinuteAverageMinorThreshold = new AtomicDouble(Double.MAX_VALUE);
		
		fiveMinuteAverageCriticalThreshold = new AtomicDouble(Double.MAX_VALUE);
		fiveMinuteAverageMajorThreshold = new AtomicDouble(Double.MAX_VALUE);
		fiveMinuteAverageMinorThreshold = new AtomicDouble(Double.MAX_VALUE);
		
		fifteenMinuteAverageCriticalThreshold = new AtomicDouble(Double.MAX_VALUE);
		fifteenMinuteAverageMajorThreshold = new AtomicDouble(Double.MAX_VALUE);
		fifteenMinuteAverageMinorThreshold = new AtomicDouble(Double.MAX_VALUE);
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
}
