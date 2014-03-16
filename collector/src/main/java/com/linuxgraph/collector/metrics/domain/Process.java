/**
 * Copyright 2014 Thomas Cashman
 */
package com.linuxgraph.collector.metrics.domain;

/**
 * A single process outputted from ps
 *
 * @author Thomas Cashman
 */
public class Process implements Comparable<Process> {
	private String user;
	private long pid;
	private double cpuUsage;
	private double memoryUsage;
	private int vsz;
	private int rss;
	private String tty;
	private String stat;
	private String start;
	private String time;
	private String command;
	
	@Override
	public int compareTo(Process o) {
		int cpu = Double.compare(o.cpuUsage, cpuUsage);
		if(cpu == 0) {
			return Double.compare(o.memoryUsage, memoryUsage);
		}
		return cpu;
	}
	
	public String getUser() {
		return user;
	}
	
	public void setUser(String user) {
		this.user = user;
	}
	
	public long getPid() {
		return pid;
	}
	
	public void setPid(long pid) {
		this.pid = pid;
	}
	
	public double getCpuUsage() {
		return cpuUsage;
	}
	
	public void setCpuUsage(double cpuUsage) {
		this.cpuUsage = cpuUsage;
	}
	
	public double getMemoryUsage() {
		return memoryUsage;
	}
	
	public void setMemoryUsage(double memoryUsage) {
		this.memoryUsage = memoryUsage;
	}
	
	public int getVsz() {
		return vsz;
	}
	
	public void setVsz(int vsz) {
		this.vsz = vsz;
	}
	public int getRss() {
		return rss;
	}
	
	public void setRss(int rss) {
		this.rss = rss;
	}
	
	public String getTty() {
		return tty;
	}
	
	public void setTty(String tty) {
		this.tty = tty;
	}
	
	public String getStat() {
		return stat;
	}
	
	public void setStat(String stat) {
		this.stat = stat;
	}
	
	public String getStart() {
		return start;
	}
	
	public void setStart(String start) {
		this.start = start;
	}
	
	public String getTime() {
		return time;
	}
	
	public void setTime(String time) {
		this.time = time;
	}
	
	public String getCommand() {
		return command;
	}
	
	public void setCommand(String command) {
		this.command = command;
	}
}
