/**
 * Copyright 2014 Thomas Cashman
 */
package com.linuxgraph.collector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application entry point
 * 
 * @author Thomas Cashman
 */
@ComponentScan
@EnableAutoConfiguration
@EnableScheduling
public class LinuxGraphCollector {
	public static void main(String[] args) throws Exception {
		SpringApplication.run(LinuxGraphCollector.class);
	}
}
