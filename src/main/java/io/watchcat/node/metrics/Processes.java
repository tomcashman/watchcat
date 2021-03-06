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

import io.watchcat.node.metrics.domain.Process;
import io.watchcat.node.util.ShellCommand;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.springframework.stereotype.Component;

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
	
	public XContentBuilder toJson(long timestamp) {
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
