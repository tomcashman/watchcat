/**
 * Copyright 2014 Thomas Cashman
 */
package com.linuxgraph.collector;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * Main application entry point
 * 
 * @author Thomas Cashman
 */
@ComponentScan(basePackages={"com.linuxgraph.collector"})
@EnableAutoConfiguration
@PropertySource("file:/etc/linux-graph/elasticsearch.properties")
public class LinuxGraphCollector {
	private static ScheduledExecutorService scheduledExecutorService;
	
	public static void main(String[] args) throws Exception {
		SpringApplication.run(LinuxGraphCollector.class);
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
}
