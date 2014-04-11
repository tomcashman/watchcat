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
package com.viridiansoftware.watchcat.node.metrics;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.springframework.stereotype.Component;

import com.viridiansoftware.watchcat.node.metrics.domain.Filesystem;
import com.viridiansoftware.watchcat.node.util.ShellCommand;

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
	private AtomicReference<LinkedList<Filesystem>> filesystems;
	
	public DiskUsage() {
		command = new ShellCommand("/bin/df --block-size=1M|awk '{print $1\",\"$2\",\"$3\",\"$4\",\"$5\",\"$6}'");
		filesystems = new AtomicReference<>(new LinkedList<Filesystem>());
	}
	
	@Override
	public void run() {
		LinkedList<Filesystem> result = new LinkedList<Filesystem>();
		String commandResult = command.execute();
		
		String [] lines = commandResult.split("\n");
		for(int i = 1; i < lines.length; i++) {
			String [] diskInfo = lines[i].split(",");
			Filesystem filesystem = new Filesystem(diskInfo[0], Integer.parseInt(diskInfo[1]), Integer.parseInt(diskInfo[2]),
					Integer.parseInt(diskInfo[3]), Integer.parseInt(diskInfo[4].replace("%", "")), diskInfo[5]);
			result.add(filesystem);
		}
		
		filesystems.set(result);
	}
	
	public List<Filesystem> getFilesystems() {
		return filesystems.get();
	}
	
	public XContentBuilder toJson(long timestamp) {
		try {
			XContentBuilder builder = XContentFactory.jsonBuilder();
			builder = builder.startObject();
			builder = builder.field("timestamp", timestamp);
			builder = builder.startArray("filesystems");
			
			Iterator<Filesystem> iterator = filesystems.get().iterator();
			while(iterator.hasNext()) {
				Filesystem filesystem = iterator.next();
				builder = builder.startObject();
				builder = builder.field("filesystem", filesystem.getFilesystem());
				builder = builder.field("size", filesystem.getSize());
				builder = builder.field("used", filesystem.getUsed());
				builder = builder.field("free", filesystem.getFree());
				builder = builder.field("percentageUsed", filesystem.getPercentageUsed());
				builder = builder.field("mountPoint", filesystem.getMountPoint());
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
		stringBuilder.append("DiskUsage [filesystems=\n");
		
		List<Filesystem> items = filesystems.get();
		Iterator<Filesystem> iterator = items.iterator();
		while(iterator.hasNext()) {
			Filesystem filesystem = iterator.next();
			stringBuilder.append(filesystem);
			stringBuilder.append("\n");
		}
		stringBuilder.append("]");
		return stringBuilder.toString();
	}
}
