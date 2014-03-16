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

import com.linuxgraph.collector.metrics.domain.ConnectionsData;
import com.linuxgraph.collector.metrics.domain.Filesystem;
import com.linuxgraph.collector.util.ShellCommand;

/**
 * Gathers network connection information from netstat
 *
 * @author Thomas Cashman
 */
@Component
public class NetworkConnections implements Runnable {
	private ShellCommand command;
	private AtomicReference<LinkedList<ConnectionsData>> connections;
	
	public NetworkConnections() {
		command = new ShellCommand("netstat -ntu | awk 'NR>2 {sub(/:[^:]+$/, \"\"); print $5}' | sort | uniq -c");
		
		connections = new AtomicReference<LinkedList<ConnectionsData>>(new LinkedList<ConnectionsData>());
	}

	@Override
	public void run() {
		String [] lines = command.execute().split("\n");
		
		LinkedList<ConnectionsData> result = new LinkedList<ConnectionsData>();
		for(int i = 0; i < lines.length; i++) {
			String [] details = lines[i].trim().split(" ");
			
			ConnectionsData connectionData = new ConnectionsData();
			connectionData.setTotal(Integer.parseInt(details[0]));
			connectionData.setAddress(details[1]);
			result.add(connectionData);
		}
		connections.set(result);
	}

	public List<ConnectionsData> getConnections() {
		return connections.get();
	}
	
	public XContentBuilder toJson(String timestamp) {
		try {
			XContentBuilder builder = XContentFactory.jsonBuilder();
			builder = builder.startObject();
			builder = builder.field("timestamp", timestamp);
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
