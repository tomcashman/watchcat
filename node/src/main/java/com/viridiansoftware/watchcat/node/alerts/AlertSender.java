/**
 * Copyright 2014 Thomas Cashman
 */
package com.viridiansoftware.watchcat.node.alerts;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.PostConstruct;

import org.elasticsearch.client.transport.TransportClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.viridiansoftware.watchcat.node.event.Criticality;

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

	private AtomicReference<List<String>> emailAddresses;
	private Properties smtpHostProperties;

	public AlertSender() {
		emailAddresses = new AtomicReference<List<String>>(
				new ArrayList<String>());
	}

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

		List<String> emailAlertRecipients = this.emailAddresses.get();
		if (emailAlertRecipients.size() > 0) {
			SMTPAlert smtpAlert = new SMTPAlert(smtpHostProperties,
					smtpUsername, smtpPassword, hostname, message);

			for (String emailAddress : emailAlertRecipients) {
				if (!smtpAlert.send(emailAddress)) {
					alertLogger = new AlertLogger(hostname, Criticality.CRITICAL,
							"Could not send email alert to " + emailAddress);
					alertLogger.log(transportClient);
				}
			}
		}
	}
}
