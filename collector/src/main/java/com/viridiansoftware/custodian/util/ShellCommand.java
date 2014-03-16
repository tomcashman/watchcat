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
package com.viridiansoftware.custodian.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Provides execution and output retrieval of shell commands
 * 
 * @author Thomas Cashman
 */
public class ShellCommand {
	private String [] command;

	/**
	 * Constructor
	 * @param command The shell command to execute
	 */
	public ShellCommand(String command) {
		this.command = new String [] { "/bin/sh", "-c", command };
	}

	/**
	 * Executes the shell command and returns its output
	 * @return An empty string on error
	 */
	public String execute() {
		StringBuffer output = new StringBuffer();

		Process p;
		try {
			p = Runtime.getRuntime().exec(command);
			p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			String line = "";
			while ((line = reader.readLine()) != null) {
				output.append(line + "\n");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return output.toString();
	}
}
