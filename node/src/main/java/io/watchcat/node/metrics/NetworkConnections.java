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

import io.watchcat.node.metrics.domain.ConnectionsData;
import io.watchcat.node.util.ShellCommand;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.springframework.stereotype.Component;

/**
 * Gathers network connection information from netstat
 *
 * @author Thomas Cashman
 */
@Component
public class NetworkConnections implements Runnable {
	private ShellCommand command;
	private AtomicReference<LinkedList<ConnectionsData>> connections;
	private AtomicLong totalConnections;
	
	public NetworkConnections() {
		command = new ShellCommand("netstat -ntu | awk 'NR>2 {sub(/:[^:]+$/, \"\"); print $5}' | sort | uniq -c");
		
		totalConnections = new AtomicLong(0);
		connections = new AtomicReference<LinkedList<ConnectionsData>>(new LinkedList<ConnectionsData>());
	}

	@Override
	public void run() {
		String [] lines = command.execute().split("\n");
		
		LinkedList<ConnectionsData> result = new LinkedList<ConnectionsData>();
		long total = 0;
		for(int i = 0; i < lines.length; i++) {
			String [] details = lines[i].trim().split(" ");
			
			ConnectionsData connectionData = new ConnectionsData();
			connectionData.setTotal(Integer.parseInt(details[0]));
			connectionData.setAddress(details[1]);
			result.add(connectionData);
			
			total += connectionData.getTotal();
		}
		connections.set(result);
		totalConnections.set(total);
	}
	
	public long getTotalConnections() {
		return totalConnections.get();
	}

	public List<ConnectionsData> getConnections() {
		return connections.get();
	}
	
	public XContentBuilder toJson(long timestamp) {
		try {
			XContentBuilder builder = XContentFactory.jsonBuilder();
			builder = builder.startObject();
			builder = builder.field("timestamp", timestamp);
			builder = builder.field("totalConnections", totalConnections.get());
			builder = builder.startArray("connections");
			
			Iterator<ConnectionsData> iterator = connections.get().iterator();
			while(iterator.hasNext()) {
				ConnectionsData connectionData = iterator.next();
				builder = builder.startObject();
				builder = builder.field("address", connectionData.getAddress());
				builder = builder.field("total", connectionData.getTotal());
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
