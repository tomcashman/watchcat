/**
 * Copyright 2014 Thomas Cashman
 */
package com.linuxgraph.collector.metrics;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.springframework.stereotype.Component;

import com.linuxgraph.collector.metrics.domain.Filesystem;
import com.linuxgraph.collector.util.ShellCommand;

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
	
	public XContentBuilder toJson(String timestamp) {
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
