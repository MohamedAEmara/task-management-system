package com.emara.task.service;

import com.emara.task.config.RabbitMQConfig;
import com.emara.task.dto.MessageDto;
import com.emara.task.model.MailStructure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmailMessageConsumer {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailMessageConsumer.class);
    
    @Autowired
    private MailService mailService;
    
    @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE)
    public void processEmailMessage(MessageDto messageDto) {
        try {
            logger.info("Processing email message for: {}", messageDto.getTo());
            
            // Create MailStructure from MessageDto
            MailStructure mailStructure = new MailStructure();
            mailStructure.setSubject(messageDto.getSubject());
            mailStructure.setMessage(messageDto.getMessage());
            
            // Send the email
            mailService.sendMail(messageDto.getTo(), mailStructure);
            
            logger.info("Successfully sent email to: {}", messageDto.getTo());
            
        } catch (Exception e) {
            logger.error("Failed to send email to {}: {}", messageDto.getTo(), e.getMessage(), e);
        }
    }
}
