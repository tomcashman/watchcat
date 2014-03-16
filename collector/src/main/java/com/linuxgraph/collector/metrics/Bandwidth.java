/**
 * Copyright 2014 Thomas Cashman
 */
package com.linuxgraph.collector.metrics;

import java.util.concurrent.atomic.AtomicLong;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.springframework.stereotype.Component;

import com.linuxgraph.collector.util.ShellCommand;

/**
 *
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
