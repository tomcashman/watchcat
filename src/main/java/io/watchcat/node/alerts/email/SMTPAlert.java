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
package io.watchcat.node.alerts.email;

import io.watchcat.node.event.Criticality;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Common interface for SMTP-based alerts
 * 
 * @author Thomas Cashman
 */
public class SMTPAlert {
	private String hostname;
	private String message;
	private Properties smtpHostProperties;
	private String smtpUsername;
	private String smtpPassword;
	private Criticality criticality;

	public SMTPAlert(Properties smtpHostProperties, String smtpUsername,
			String smtpPassword, String hostname, Criticality criticality, String message) {
		this.hostname = hostname;
		this.message = message;
		this.criticality = criticality;

		this.smtpHostProperties = smtpHostProperties;
		this.smtpUsername = smtpUsername;
		this.smtpPassword = smtpPassword;
	}

	/**
	 * Sends the alert to an email address
	 * 
	 * @param emailAddress
	 *            The email address which should receive the alert
	 * @return True on success
	 */
	public boolean send(String emailAddress) {
		try {
			Session session = Session.getInstance(smtpHostProperties,
					new javax.mail.Authenticator() {
						protected PasswordAuthentication getPasswordAuthentication() {
							return new PasswordAuthentication(smtpUsername,
									smtpPassword);
						}
					});

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress("from-email@gmail.com"));
			message.setRecipients(Message.RecipientType.TO,
					InternetAddress.parse(emailAddress));
			message.setSubject("[" + hostname + "][" + this.criticality + "] " + this.message);
			message.setText(this.criticality + " watchcat alert from '" + hostname + "'\n" + this.message);

			Transport.send(message);
			return true;
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		return false;
	}
}
