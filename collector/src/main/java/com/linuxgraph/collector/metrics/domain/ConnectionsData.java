/**
 * Copyright 2014 Thomas Cashman
 */
package com.linuxgraph.collector.metrics.domain;

/**
 * A single row of connection data outputted from netstat
 *
 * @author Thomas Cashman
 */
public class ConnectionsData {
	private int total;
	private String address;
	
	public int getTotal() {
		return total;
	}
	
	public void setTotal(int total) {
		this.total = total;
	}
	
	public String getAddress() {
		return address;
	}
	
	public void setAddress(String address) {
		this.address = address;
	}
}
