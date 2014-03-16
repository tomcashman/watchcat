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
package com.viridiansoftware.custodian.collector.metrics;

import java.util.concurrent.atomic.AtomicReference;

import org.elasticsearch.common.util.concurrent.jsr166e.extra.AtomicDouble;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.springframework.stereotype.Component;

import com.viridiansoftware.custodian.collector.util.ShellCommand;

/**
 * Gathers the load average and number of CPU cores of the system
 *
 * @author Thomas Cashman
 */
@Component
public class LoadAverage implements Runnable {
	private ShellCommand numberOfCoresPrimaryCommand, numberOfCoresSecondaryCommand;
	private ShellCommand loadAverageCommand;
	private AtomicDouble oneMinuteAverage;
	private AtomicDouble fiveMinuteAverage;
	private AtomicDouble fifteenMinuteAverage;
	private AtomicReference<String> numberOfCpuCores;
	
	public LoadAverage() {
		numberOfCoresPrimaryCommand = new ShellCommand("/bin/grep -c ^processor /proc/cpuinfo");
		numberOfCoresSecondaryCommand = new ShellCommand("/usr/bin/nproc");
		loadAverageCommand = new ShellCommand("/bin/cat /proc/loadavg | /usr/bin/awk '{print $1\",\"$2\",\"$3}'");
		
		numberOfCpuCores = new AtomicReference<String>();
		oneMinuteAverage = new AtomicDouble();
		fiveMinuteAverage = new AtomicDouble();
		fifteenMinuteAverage = new AtomicDouble();
	}
	
	@Override
	public void run() {
		String numberOfCoresResult = numberOfCoresPrimaryCommand.execute();
		if(numberOfCoresResult == null || numberOfCoresResult.length() == 0) {
			numberOfCoresResult = numberOfCoresSecondaryCommand.execute();
			if(numberOfCoresResult == null || numberOfCoresResult.length() == 0) {
				numberOfCoresResult = "Unknown";
			}
		}
		numberOfCpuCores.set(numberOfCoresResult.replace("\n", ""));
		
		String [] loadAverages = loadAverageCommand.execute().split(",");
		oneMinuteAverage.set(Double.parseDouble(loadAverages[0]));
		fiveMinuteAverage.set(Double.parseDouble(loadAverages[1]));
		fifteenMinuteAverage.set(Double.parseDouble(loadAverages[2]));
	}
	
	public String getNumberOfCpuCores() {
		return numberOfCpuCores.get();
	}

	public double getOneMinuteAverage() {
		return oneMinuteAverage.get();
	}

	public double getFiveMinuteAverage() {
		return fiveMinuteAverage.get();
	}

	public double getFifteenMinuteAverage() {
		return fifteenMinuteAverage.get();
	}
	
	public XContentBuilder toJson(String timestamp) {
		try {
			XContentBuilder builder = XContentFactory.jsonBuilder();
			builder = builder.startObject();
			builder = builder.field("timestamp", timestamp);
			builder = builder.field("cpuCores", getNumberOfCpuCores());
			builder = builder.field("oneMinuteAverage", getOneMinuteAverage());
			builder = builder.field("fiveMinuteAverage", getFiveMinuteAverage());
			builder = builder.field("fifteenMinuteAverage", getFifteenMinuteAverage());
			builder = builder.endObject();
			return builder;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String toString() {
		return "LoadAverage [oneMinuteAverage=" + oneMinuteAverage.get()
				+ ", fiveMinuteAverage=" + fiveMinuteAverage.get()
				+ ", fifteenMinuteAverage=" + fifteenMinuteAverage.get()
				+ ", numberOfCpuCores=" + numberOfCpuCores.get() + "]";
	}
}
