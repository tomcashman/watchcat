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

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for {@link NetworkConnections}
 *
 * @author Thomas Cashman
 */
public class NetworkConnectionsTest implements Runnable {
	private NetworkConnections networkConnections;
	
	private ServerSocket server;
	private Socket client;
	private Thread serverThread;
	
	@Before
	public void setup() throws IOException {
		networkConnections = new NetworkConnections();
		serverThread = new Thread(this);
		
		createClientServer();
	}
	
	@After
	public void teardown() {
		shutdownClientServer();
	}

	@Test
	public void testRun() {
		Assert.assertEquals(0, networkConnections.getTotalConnections());	
		Assert.assertEquals(0, networkConnections.getConnections().size());
		networkConnections.run();
		Assert.assertEquals(true, networkConnections.getTotalConnections() > 0);	
		Assert.assertEquals(true, networkConnections.getConnections().size() > 0);
	}

	@Test
	public void testToJsonWithNoData() throws IOException {
		long timestamp = System.currentTimeMillis();
		XContentBuilder content = networkConnections.toJson(timestamp);
		String json = content.string();
		
		Assert.assertEquals(true, json.contains("\"timestamp\":" + timestamp));
		Assert.assertEquals(true, json.contains("\"totalConnections\":" + networkConnections.getTotalConnections()));
		Assert.assertEquals(true, json.contains("\"connections\":[]"));
	}

	@Test
	public void testToJsonWithData() throws IOException {
		networkConnections.run();
		
		long timestamp = System.currentTimeMillis();
		XContentBuilder content = networkConnections.toJson(timestamp);
		String json = content.string();
		
		Assert.assertEquals(true, json.contains("\"timestamp\":" + timestamp));
		Assert.assertEquals(true, json.contains("\"totalConnections\":" + networkConnections.getTotalConnections()));
		Assert.assertEquals(true, json.contains("\"connections\":[{"));
		Assert.assertEquals(true, json.endsWith("}]}"));
	}
	
	private void createClientServer() throws IOException {
		int port = 10001;
		server = new ServerSocket(port);
		serverThread.start();
		client = new Socket("localhost", port);
	}
	
	private void shutdownClientServer() {
		try {
			client.close();
		} catch (IOException e1) {
		}
		try {
			server.close();
		} catch (IOException e) {
		}
	}

	@Override
	public void run() {
		while(server.isBound() && !server.isClosed()) {
			try {
				server.accept();
			} catch (IOException e) {
			}
		}
	}
}
