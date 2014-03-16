/**
 * Copyright 2014 Thomas Cashman
 */
package com.linuxgraph.collector.metrics;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.springframework.stereotype.Component;

import com.linuxgraph.collector.metrics.domain.Filesystem;
import com.linuxgraph.collector.metrics.domain.Process;
import com.linuxgraph.collector.util.ShellCommand;

/**
 *
 *
 * @author Thomas Cashman
 */
@Component
public class Processes implements Runnable {
	private ShellCommand command;
	private AtomicReference<LinkedList<Process>> processes;
	
	public Processes() {
		command = new ShellCommand("/bin/ps aux | /usr/bin/awk 'NR>1{print $1\",\"$2\",\"$3\",\"$4\",\"$5\",\"$6\",\"$7\",\"$8\",\"$9\",\"$10\",\"$11}'");
		
		processes = new AtomicReference<LinkedList<Process>>(new LinkedList<Process>());
	}

	@Override
	public void run() {
		String [] lines = command.execute().split("\n");
		
		LinkedList<Process> result = new LinkedList<Process>();
		for(int i = 0; i < lines.length; i++) {
			String [] details = lines[i].split(",");
			
			Process process = new Process();
			process.setUser(details[0]);
			process.setPid(Long.parseLong(details[1]));
			process.setCpuUsage(Double.parseDouble(details[2]));
			process.setMemoryUsage(Double.parseDouble(details[3]));
			process.setVsz(Integer.parseInt(details[4]));
			process.setRss(Integer.parseInt(details[5]));
			process.setTty(details[6]);
			process.setStat(details[7]);
			process.setStart(details[8]);
			process.setTime(details[9]);
			process.setCommand(details[10].trim());
			result.add(process);
		}
		Collections.sort(result);
		processes.set(result);
	}

	public List<Process> getProcesses() {
		return processes.get();
	}
	
	public XContentBuilder toJson(String timestamp) {
		try {
			XContentBuilder builder = XContentFactory.jsonBuilder();
			builder = builder.startObject();
			builder = builder.field("timestamp", timestamp);
			builder = builder.startArray("processes");
			
			Iterator<Process> iterator = processes.get().iterator();
			while(iterator.hasNext()) {
				Process process = iterator.next();
				builder = builder.startObject();
				builder = builder.field("user", process.getUser());
				builder = builder.field("pid", process.getPid());
				builder = builder.field("cpu", process.getCpuUsage());
				builder = builder.field("memory", process.getMemoryUsage());
				builder = builder.field("vsz", process.getVsz());
				builder = builder.field("rss", process.getRss());
				builder = builder.field("tty", process.getTty());
				builder = builder.field("stat", process.getStat());
				builder = builder.field("start", process.getStart());
				builder = builder.field("time", process.getTime());
				builder = builder.field("command", process.getCommand());
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
}
