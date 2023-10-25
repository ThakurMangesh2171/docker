package com.vassarlabs.gp.utils;

import com.vassarlabs.gp.constants.Constants;
import com.vassarlabs.gp.service.api.IEmailService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.util.Objects;

@Component
public class EmailService implements IEmailService {

	 @Autowired
	 private JavaMailSender emailSender;

	 @Value("${spring.mail.username:false}")
	 private String userName;

	 @Value("${spring.mail.required}")
	 private String emailApiRequired;

	 //TODO : Uncomment this for Akeyless
//	@Value("${email.from}")
//	private String emailFrom;

	@Value("${rfp.mail.cc}")
	private String cc;

	@Value("${email.maxAttempts}")
	private int emailMaxAttempts;

	 private static final Logger LOGGER = LogManager.getLogger(EmailService.class);




	@Override
	@Async
	public Boolean sendSimpleMessage  (String to, String subject, String text) throws MailException {

		   //Check email sending feature enable or not
			if(emailApiRequired.equalsIgnoreCase(Constants.ISTRUE)) {
				LOGGER.debug("Email feature enabled");
				SimpleMailMessage message = new SimpleMailMessage();
				message.setFrom(userName);
				message.setTo(to);
				message.setSubject(subject);
				message.setText(text);
				emailSender.send(message);
				return true;
			}

		return false;
	}

	@Override
//	@Async
	public Boolean sendMessageWithAttachment(String[] to, String subject, String text, String pathToAttachment) {

		int numberOfAttempts = 0;
		boolean isMailSent = false;
		while(!isMailSent && numberOfAttempts<emailMaxAttempts) {
			if (emailApiRequired.equalsIgnoreCase(Constants.ISTRUE)) {
				try {
					numberOfAttempts++;
					LOGGER.info("Sending Email :: Attempt :: "+numberOfAttempts);
					MimeMessage message = emailSender.createMimeMessage();
					// pass 'true' to the constructor to create a multipart message
					MimeMessageHelper helper = new MimeMessageHelper(message, true);
//				TODO : Uncomment this for Akeyless
//				helper.setFrom(emailFrom);
					helper.setTo(to);
					if (cc != null && !cc.trim().isEmpty()) {
						String[] mailCC = cc.split(Constants.COMMA);
						helper.setCc(mailCC);
					}
					helper.setSubject(subject);
					helper.setText(text, true);


//	            FileSystemResource file = new FileSystemResource(new File(pathToAttachment));
//	            helper.addAttachment("response rfp excel", file);

					// Attach the Excel file
					FileSystemResource file = new FileSystemResource(new File(pathToAttachment));
					helper.addAttachment(file.getFilename(), file, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

					emailSender.send(message);
					isMailSent = true;
				} catch (Exception e) {
					if(numberOfAttempts>=emailMaxAttempts) {
						LOGGER.error(e.getMessage());
						e.printStackTrace();
						return false;
					}
				}

			}
		}

		return isMailSent;
	}




}
