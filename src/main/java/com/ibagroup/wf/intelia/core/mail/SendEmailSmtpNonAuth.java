package com.ibagroup.wf.intelia.core.mail;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.lang3.StringUtils;

public class SendEmailSmtpNonAuth {

	private String contentType;
	private String smtphost;
	private Map<String, File> attachments;
	private String emailTimeout;

	public SendEmailSmtpNonAuth(String smtphost) {
		this(smtphost, "");
	}

	public SendEmailSmtpNonAuth(String smtphost, String emailTimeout) {
		this.smtphost = smtphost;
		this.attachments = new HashMap<>();
		this.emailTimeout = emailTimeout;
	}

	public boolean sendSimpleEmail(String from, String to, String body, String subject) {
		this.contentType = "text/plain";
		return sendEmail(from, to, body, subject);
	}

	public boolean sendHtmlEmail(String from, String to, String body, String subject) {
		this.contentType = "text/html";
		return sendEmail(from, to, body, subject);
	}

	public boolean sendHtmlEmailToListRecipient(String from, List<String> toList, List<String> ccList, String emailBody,
			String subject) {
		this.contentType = "text/html";
		return sendEmailToListRecipient(from, toList, ccList, emailBody, subject);
	}

	public boolean sendHtmlEmailToRecipients(String from, String to, String cc, String bcc, String emailBody,
			String subject) {
		this.contentType = "text/html";
		return sendEmailToRecipients(from, to, cc, bcc, emailBody, subject);
	}

	public boolean sendHtmlEmailWithTempFileAttachment(String from, String to, String body, String subject) {
		this.contentType = "text/html";
		return sendEmailWithTempFileAttachment(from, to, body, subject);
	}

	public boolean sendHtmlEmailWithTempFileAttachmentToListRecipient(String from, List<String> toList,
			List<String> ccList, String emailBody, String subject) {
		this.contentType = "text/html";
		return sendEmailWithTempFileAttachmentToListRecipient(from, toList, ccList, emailBody, subject);
	}

	private Properties prepareProperties() {
		Properties props = System.getProperties();
		props.setProperty("mail.smtp.host", smtphost);

		if (StringUtils.isNotBlank(emailTimeout)) {
			props.setProperty("mail.smtp.timeout", emailTimeout);
			props.setProperty("mail.smtp.connectiontimeout", emailTimeout);
		}

		return props;
	}

	private boolean sendEmailWithTempFileAttachment(String from, String to, String body, String subject) {
		Properties props = prepareProperties();
		Session session = Session.getInstance(props);

		try {
			MimeMessage message = new MimeMessage(session);

			message.setFrom(new InternetAddress(from));

			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
			message.setSubject(subject);

			if (!attachments.isEmpty()) {
				Multipart multipart = new MimeMultipart();

				for (Entry<String, File> entr : attachments.entrySet()) {
					MimeBodyPart attachmentBodyPart = new MimeBodyPart();

					attachmentBodyPart.attachFile(new File(String.valueOf(entr.getValue())));
					multipart.addBodyPart(attachmentBodyPart);
				}

				MimeBodyPart messageBodyPart = new MimeBodyPart();
				messageBodyPart.setContent(body, getContentType());
				multipart.addBodyPart(messageBodyPart);

				message.setContent(multipart);
			} else {
				message.setContent(body, getContentType());
			}

			Transport transport = session.getTransport("smtp");
			transport.connect();
			transport.sendMessage(message, InternetAddress.parse(to));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return true;
	}

	private boolean sendEmailWithTempFileAttachmentToListRecipient(String from, List<String> toList,
			List<String> ccList, String body, String subject) {
		Properties props = prepareProperties();
		Session session = Session.getInstance(props);

		try {
			MimeMessage message = new MimeMessage(session);

			message.setFrom(new InternetAddress(from));

			List<InternetAddress> listInternetAddressTO = new ArrayList<>();
			for (String to : toList) {
				listInternetAddressTO.add(new InternetAddress(to));
				message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			}

			for (String cc : ccList) {
				message.addRecipient(Message.RecipientType.CC, new InternetAddress(cc));
			}

			message.setSubject(subject);

			if (!attachments.isEmpty()) {
				Multipart multipart = new MimeMultipart();

				for (Entry<String, File> entr : attachments.entrySet()) {
					MimeBodyPart attachmentBodyPart = new MimeBodyPart();

					attachmentBodyPart.attachFile(new File(String.valueOf(entr.getValue())));
					multipart.addBodyPart(attachmentBodyPart);
				}

				MimeBodyPart messageBodyPart = new MimeBodyPart();
				messageBodyPart.setContent(body, getContentType());
				multipart.addBodyPart(messageBodyPart);

				message.setContent(multipart);
			} else {
				message.setContent(body, getContentType());
			}

			Transport transport = session.getTransport("smtp");
			transport.connect();
			transport.sendMessage(message,
					listInternetAddressTO.toArray(new InternetAddress[listInternetAddressTO.size()]));
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return true;
	}

	private boolean sendEmail(String from, String to, String body, String subject) {
		Properties props = prepareProperties();
		Session session = Session.getInstance(props);

		try {
			MimeMessage message = new MimeMessage(session);

			message.setFrom(new InternetAddress(from));

			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
			message.setSubject(subject);

			if (!attachments.isEmpty()) {
				Multipart multipart = new MimeMultipart();

				for (Entry<String, File> entr : attachments.entrySet()) {
					MimeBodyPart attachmentBodyPart = new MimeBodyPart();

					attachmentBodyPart.setDataHandler(new DataHandler(new FileDataSource(entr.getValue())));
					attachmentBodyPart.setFileName(entr.getKey());

					multipart.addBodyPart(attachmentBodyPart);
				}

				MimeBodyPart messageBodyPart = new MimeBodyPart();
				messageBodyPart.setText(body, StandardCharsets.UTF_8.name());
				multipart.addBodyPart(messageBodyPart);

				message.setContent(multipart, getContentType());
			} else {
				message.setContent(body, getContentType());
			}

			Transport transport = session.getTransport("smtp");
			transport.connect();
			transport.sendMessage(message, InternetAddress.parse(to));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return true;
	}

	private boolean sendEmailToListRecipient(String from, List<String> toList, List<String> ccList, String emailBody,
			String subject) {
		Properties props = prepareProperties();
		Session session = Session.getInstance(props);

		try {
			MimeMessage message = new MimeMessage(session);

			message.setFrom(new InternetAddress(from));

			List<InternetAddress> listInternetAddressTO = new ArrayList<>();
			for (String to : toList) {
				listInternetAddressTO.add(new InternetAddress(to));
				message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			}

			for (String cc : ccList) {
			    listInternetAddressTO.add(new InternetAddress(cc));
				message.addRecipient(Message.RecipientType.CC, new InternetAddress(cc));
			}

			message.setSubject(subject);

			if (!attachments.isEmpty()) {
				Multipart multipart = new MimeMultipart();

				for (Entry<String, File> entr : attachments.entrySet()) {
					MimeBodyPart attachmentBodyPart = new MimeBodyPart();

					attachmentBodyPart.setDataHandler(new DataHandler(new FileDataSource(entr.getValue())));
					attachmentBodyPart.setFileName(entr.getKey());

					multipart.addBodyPart(attachmentBodyPart);
				}

				MimeBodyPart messageBodyPart = new MimeBodyPart();
				messageBodyPart.setText(emailBody, StandardCharsets.UTF_8.name());
				multipart.addBodyPart(messageBodyPart);

				message.setContent(multipart, getContentType());
			} else {
				message.setContent(emailBody, getContentType());
			}

			Transport transport = session.getTransport("smtp");
			transport.connect();
			transport.sendMessage(message,
					listInternetAddressTO.toArray(new InternetAddress[listInternetAddressTO.size()]));
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return true;
	}

	private boolean sendEmailToRecipients(String from, String to, String cc, String bcc, String body,
			String subject) {
		Properties props = System.getProperties();
		props.setProperty("mail.smtp.host", smtphost);

		Session session = Session.getInstance(props);

		try {
			MimeMessage message = new MimeMessage(session);

			message.setFrom(new InternetAddress(from));

			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));

			if (cc != null) {
				message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(cc));
			}

			if (bcc != null) {
				message.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(bcc));
			}

			message.setSubject(subject);

			if (!attachments.isEmpty()) {
				Multipart multipart = new MimeMultipart();

				for (Entry<String, File> entr : attachments.entrySet()) {
					MimeBodyPart attachmentBodyPart = new MimeBodyPart();

					attachmentBodyPart.setDataHandler(new DataHandler(new FileDataSource(entr.getValue())));
					attachmentBodyPart.setFileName(entr.getKey());

					multipart.addBodyPart(attachmentBodyPart);
				}

				MimeBodyPart messageBodyPart = new MimeBodyPart();
				messageBodyPart.setText(body, StandardCharsets.UTF_8.name());
				multipart.addBodyPart(messageBodyPart);

				message.setContent(multipart, getContentType());
			} else {
				message.setContent(body, getContentType());
			}

			Transport transport = session.getTransport("smtp");
			transport.connect();
			transport.sendMessage(message, InternetAddress.parse(to));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return true;
	}

	public void addAttachment(String fileName, File file) {
		this.attachments.put(fileName, file);
	}

	public String getContentType() {
		return contentType + "; charset=utf-8";
	}
}