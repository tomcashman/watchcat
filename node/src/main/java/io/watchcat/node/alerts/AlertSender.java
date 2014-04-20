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
package io.watchcat.node.alerts;

import io.watchcat.node.alerts.email.EmailAddressPoller;
import io.watchcat.node.alerts.email.SMTPAlert;
import io.watchcat.node.event.Criticality;

import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.elasticsearch.client.transport.TransportClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 *
 * @author Thomas Cashman
 */
@Component
public class AlertSender {
	@Autowired
	private TransportClient transportClient;
	@Autowired
	@Qualifier("hostname")
	private String hostname;
	@Autowired
	private EmailAddressPoller emailAddressPoller;
	
	@Value("${smtp.host}")
	private String smtpHost;
	@Value("${smtp.port}")
	private String smtpPort;
	@Value("${smtp.tls}")
	private String smtpTls;
	@Value("${smtp.username}")
	private String smtpUsername;
	@Value("${smtp.password}")
	private String smtpPassword;

	private Properties smtpHostProperties;

	@PostConstruct
	public void postConstruct() {
		smtpHostProperties = new Properties();
		smtpHostProperties.put("mail.smtp.auth", "true");
		smtpHostProperties.put("mail.smtp.starttls.enable", smtpTls);
		smtpHostProperties.put("mail.smtp.host", smtpHost);
		smtpHostProperties.put("mail.smtp.port", smtpPort);
	}

	public void sendAlert(Criticality criticality, String message) {
		AlertLogger alertLogger = new AlertLogger(hostname, criticality, message);
		alertLogger.log(transportClient);

		List<String> emailAlertRecipients = emailAddressPoller.getEmailAddresses();
		if (emailAlertRecipients.size() > 0 && smtpHost != null && smtpHost.length() > 0) {
			SMTPAlert smtpAlert = new SMTPAlert(smtpHostProperties,
					smtpUsername, smtpPassword, hostname, criticality, message);

			for (String emailAddress : emailAlertRecipients) {
				if (!smtpAlert.send(emailAddress)) {
					alertLogger = new AlertLogger(hostname, Criticality.CRITICAL,
							"Could not send email alert to " + emailAddress);
					alertLogger.log(transportClient);
				}
			}
		}
	}

	public Properties getSmtpHostProperties() {
		return smtpHostProperties;
	}

	public void setTransportClient(TransportClient transportClient) {
		this.transportClient = transportClient;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public void setEmailAddressPoller(EmailAddressPoller emailAddressPoller) {
		this.emailAddressPoller = emailAddressPoller;
	}

	public void setSmtpHost(String smtpHost) {
		this.smtpHost = smtpHost;
	}

	public void setSmtpPort(String smtpPort) {
		this.smtpPort = smtpPort;
	}

	public void setSmtpTls(String smtpTls) {
		this.smtpTls = smtpTls;
	}

	public void setSmtpUsername(String smtpUsername) {
		this.smtpUsername = smtpUsername;
	}

	public void setSmtpPassword(String smtpPassword) {
		this.smtpPassword = smtpPassword;
	}
}
