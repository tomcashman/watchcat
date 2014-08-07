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

import io.watchcat.node.util.ShellCommand;

import java.util.concurrent.atomic.AtomicInteger;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.springframework.stereotype.Component;

/**
 * Gathers the current RAM and swap usage of the system
 *
 * @author Thomas Cashman
 */
@Component
public class MemoryUsage implements Runnable {
	private ShellCommand command;
	private AtomicInteger totalMemory;
	private AtomicInteger totalSwap;
	private AtomicInteger usedMemory;
	private AtomicInteger usedSwap;
	
	public MemoryUsage() {
		command = new ShellCommand("/usr/bin/free -tmo | /usr/bin/awk '{print $1\",\"$2\",\"$3-$6-$7\",\"$4+$6+$7}'");
		
		totalMemory = new AtomicInteger();
		totalSwap = new AtomicInteger();
		usedMemory = new AtomicInteger();
		usedSwap = new AtomicInteger();
	}
	
	@Override
	public void run() {
		String result = command.execute();
		
		String [] lines = result.split("\n");
		
		String [] memory = lines[1].split(",");
		String [] swap = lines[2].split(",");
		
		totalMemory.set(Integer.parseInt(memory[1]));
		totalSwap.set(Integer.parseInt(swap[1]));
		
		usedMemory.set(Integer.parseInt(memory[2]));
		usedSwap.set(Integer.parseInt(swap[2]));
	}
	
	public XContentBuilder toJson(long timestamp) {
		try {
			XContentBuilder builder = XContentFactory.jsonBuilder();
			builder = builder.startObject();
			builder = builder.field("timestamp", timestamp);
			builder = builder.field("totalMemory", getTotalMemory());
			builder = builder.field("totalSwap", getTotalSwap());
			builder = builder.field("usedMemory", getUsedMemory());
			builder = builder.field("usedSwap", getUsedSwap());
			builder = builder.endObject();
			return builder;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String toString() {
		return "MemoryUsage [totalMemory=" + totalMemory.get() + "mb, totalSwap="
				+ totalSwap.get() + "mb, usedMemory=" + usedMemory.get() + "mb, usedSwap="
				+ usedSwap.get() + "mb]";
	}

	public int getTotalMemory() {
		return totalMemory.get();
	}

	public void setTotalMemory(int totalMemory) {
		this.totalMemory.set(totalMemory);
	}

	public int getTotalSwap() {
		return totalSwap.get();
	}

	public void setTotalSwap(int totalSwap) {
		this.totalSwap.set(totalSwap);
	}

	public int getUsedMemory() {
		return usedMemory.get();
	}

	public void setUsedMemory(int usedMemory) {
		this.usedMemory.set(usedMemory);
	}

	public int getUsedSwap() {
		return usedSwap.get();
	}

	public void setUsedSwap(int usedSwap) {
		this.usedSwap.set(usedSwap);
	}
}