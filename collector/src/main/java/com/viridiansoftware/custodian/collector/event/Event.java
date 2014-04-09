/**
 * Copyright 2014 Thomas Cashman
 */
package com.viridiansoftware.custodian.collector.event;

/**
 * Common interface for system events
 *
 * @author Thomas Cashman
 */
public interface Event {

	/**
	 * Begin the event
	 */
	public void begin();
	
	/**
	 * End the event
	 */
	public void end();
}
