package com.vassarlabs.gp.service.api;




public interface IEmailService {
	
	
	/**
	 * This method sends a simple email message without any attachments 
	 * @param to
	 * @param subject
	 * @param mail text
	 * @return
	 */
	
    Boolean sendSimpleMessage(String to,
                           String subject,
                           String text);
    
//    void sendSimpleMessageUsingTemplate(String to,
//                                        String subject,
//                                        SimpleMailMessage template,
//                                        String ...templateArgs);
    
    Boolean sendMessageWithAttachment(String[] to,
                                   String subject,
                                   String text,
                                   String pathToAttachment);
}