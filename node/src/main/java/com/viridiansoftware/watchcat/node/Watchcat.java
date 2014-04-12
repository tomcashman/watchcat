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
package com.viridiansoftware.watchcat.node;

import java.net.InetAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.annotation.PreDestroy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import com.viridiansoftware.watchcat.node.util.ShellCommand;

/**
 * Main application entry point
 * 
 * @author Thomas Cashman
 */
@ComponentScan(basePackages={"com.viridiansoftware.watchcat"})
@EnableAutoConfiguration
@PropertySource({ "file:/etc/watchcat/node.properties", "file:/etc/watchcat/smtp.properties" })
public class Watchcat {
	private static ScheduledExecutorService scheduledExecutorService;
	
	public static void main(String[] args) throws Exception {
		SpringApplication.run(Watchcat.class).registerShutdownHook();
	}

	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}
	
	@Bean
	public static ScheduledExecutorService scheduledExecutorService() {
		if(scheduledExecutorService == null) {
			scheduledExecutorService = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
		}
		return scheduledExecutorService;
	}
	
	@Bean
	public String hostname() {
		ShellCommand getHostname = new ShellCommand("cat /etc/hostname");
		String hostname = getHostname.execute().replace("\n", "");

		if (hostname == null || hostname.length() == 0) {
			try {
				hostname = InetAddress.getLocalHost().getHostName();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return hostname;
	}
	
	@PreDestroy
	public void preDestroy() {
		scheduledExecutorService.shutdown();
	}
}
