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
package com.viridiansoftware.watchcat.node.metrics.domain;


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
