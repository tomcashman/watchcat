/**
 * Copyright 2014 Thomas Cashman
 */
package com.linuxgraph.collector.metrics.domain;


/**
 * A single filesystem outputted from df
 *
 * @author Thomas Cashman
 */
public class Filesystem {
	private String filesystem;
	private int size;
	private int used;
	private int free;
	private int percentageUsed;
	private String mountPoint;
	
	public Filesystem(String filesystem, int size, int used, int free,
			int percentageUsed, String mountPoint) {
		this.filesystem = filesystem;
		this.size = size;
		this.used = used;
		this.free = free;
		this.percentageUsed = percentageUsed;
		this.mountPoint = mountPoint;
	}

	public String getFilesystem() {
		return filesystem;
	}

	public int getSize() {
		return size;
	}

	public int getUsed() {
		return used;
	}

	public int getFree() {
		return free;
	}

	public int getPercentageUsed() {
		return percentageUsed;
	}

	public String getMountPoint() {
		return mountPoint;
	}

	@Override
	public String toString() {
		return "Filesystem [filesystem=" + filesystem + ", size=" + size
				+ "M, used=" + used + "M, available=" + free
				+ "M, percentageUsed=" + percentageUsed + "%, mountPoint="
				+ mountPoint + "]";
	}
}
