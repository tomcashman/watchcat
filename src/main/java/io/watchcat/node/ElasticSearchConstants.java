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
package io.watchcat.node;

/**
 *
 *
 * @author Thomas Cashman
 */
public class ElasticSearchConstants {
	public static String WATCHCAT_INDEX = "watchcat";
	public static String WATCHCAT_ALERTS_INDEX = "watchcat-alerts";
	
	public static String HOST_TYPE = "host";
	public static String THRESHOLD_TYPE = "threshold";
	public static String ALERT_DESTINATION_TYPE = "alert-destination";
	
	public static String BANDWIDTH = "bandwidth";
	public static String DISKS = "disks";
	public static String LOAD_AVERAGE = "load";
	public static String MEMORY_USAGE = "memory";
	public static String NETWORK_CONNECTIONS = "connections";
	public static String PROCESSES = "processes";
	
	public static String EMAIL_ADDRESSES = "emailaddresses";
}
