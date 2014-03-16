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

import java.util.concurrent.atomic.AtomicLong;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.springframework.stereotype.Component;

import com.viridiansoftware.custodian.collector.util.ShellCommand;

/**
 * Gathers RX and TX statistics
 *
 * @author Thomas Cashman
 */
@Component
public class Bandwidth implements Runnable {
	private ShellCommand txCommand, rxCommand;
	private AtomicLong previousTxBytes;
	private AtomicLong previousRxBytes;
	private AtomicLong txBytes;
	private AtomicLong rxBytes;
	
	public Bandwidth() {
		txCommand = new ShellCommand("cat /sys/class/net/eth0/statistics/tx_bytes");
		rxCommand = new ShellCommand("cat /sys/class/net/eth0/statistics/rx_bytes");
		
		previousRxBytes = new AtomicLong();
		previousTxBytes = new AtomicLong();
		
		rxBytes = new AtomicLong();
		txBytes = new AtomicLong();
	}

	@Override
	public void run() {
		String rxBytesResult = rxCommand.execute().trim();
		String txBytesResult = txCommand.execute().trim();
		
		long currentRxBytes = Long.parseLong(rxBytesResult);
		long currentTxBytes = Long.parseLong(txBytesResult);
		
		rxBytes.set(currentRxBytes - previousRxBytes.get());
		txBytes.set(currentTxBytes - previousTxBytes.get());
		
		previousRxBytes.set(currentRxBytes);
		previousTxBytes.set(currentTxBytes);
	}

	public long getTxBandwidth() {
		return txBytes.get();
	}
	
	public long getRxBandwidth() {
		return rxBytes.get();
	}
	
	public XContentBuilder toJson(String timestamp) {
		try {
			XContentBuilder builder = XContentFactory.jsonBuilder();
			builder = builder.startObject();
			builder = builder.field("timestamp", timestamp);
			builder = builder.field("rx", getRxBandwidth());
			builder = builder.field("tx", getTxBandwidth());
			builder = builder.endObject();
			return builder;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
