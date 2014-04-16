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

import io.watchcat.node.metrics.domain.Disk;
import io.watchcat.node.util.ShellCommand;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.springframework.stereotype.Component;

/**
 * Gathers disk usage figures for the system.
 * 
 * Note: All data is retrieved in megabytes
 *
 * @author Thomas Cashman
 */
@Component
public class DiskUsage implements Runnable {
	private ShellCommand command;
	private AtomicReference<LinkedList<Disk>> disks;
	
	public DiskUsage() {
		command = new ShellCommand("/bin/df --block-size=1M|awk '{print $1\",\"$2\",\"$3\",\"$4\",\"$5\",\"$6}'");
		disks = new AtomicReference<>(new LinkedList<Disk>());
	}
	
	@Override
	public void run() {
		LinkedList<Disk> result = new LinkedList<Disk>();
		String commandResult = command.execute();
		
		String [] lines = commandResult.split("\n");
		for(int i = 1; i < lines.length; i++) {
			String [] diskInfo = lines[i].split(",");
			Disk disk = new Disk(diskInfo[0], Integer.parseInt(diskInfo[1]), Integer.parseInt(diskInfo[2]),
					Integer.parseInt(diskInfo[3]), Integer.parseInt(diskInfo[4].replace("%", "")), diskInfo[5]);
			result.add(disk);
		}
		
		disks.set(result);
	}

	public XContentBuilder toJson(long timestamp) {
		try {
			XContentBuilder builder = XContentFactory.jsonBuilder();
			builder = builder.startObject();
			builder = builder.field("timestamp", timestamp);
			builder = builder.startArray("filesystems");
			
			Iterator<Disk> iterator = disks.get().iterator();
			while(iterator.hasNext()) {
				Disk disk = iterator.next();
				builder = builder.startObject();
				builder = builder.field("filesystem", disk.getDisk());
				builder = builder.field("size", disk.getSize());
				builder = builder.field("used", disk.getUsed());
				builder = builder.field("free", disk.getFree());
				builder = builder.field("percentageUsed", disk.getPercentageUsed());
				builder = builder.field("mountPoint", disk.getMountPoint());
				builder = builder.endObject();
			}
			
			builder = builder.endArray();
			builder = builder.endObject();
			return builder;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("DiskUsage [disk=\n");
		
		List<Disk> items = disks.get();
		Iterator<Disk> iterator = items.iterator();
		while(iterator.hasNext()) {
			Disk disk = iterator.next();
			stringBuilder.append(disk);
			stringBuilder.append("\n");
		}
		stringBuilder.append("]");
		return stringBuilder.toString();
	}
	
	public List<Disk> getDisks() {
		return disks.get();
	}
	
	public void setDisks(LinkedList<Disk> disks) {
		this.disks.set(disks);
	}
}
