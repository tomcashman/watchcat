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
package com.viridiansoftware.custodian.collector.metrics.domain;

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
