package com.emara.task.controller;

import com.emara.task.service.TaskNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);
    
    @Autowired
    private TaskNotificationService taskNotificationService;
    
    @PostMapping("/send-incomplete-tasks")
    public ResponseEntity<String> sendIncompleteTasksNotifications() {
        logger.info("Manual trigger received for incomplete task notifications");
        
        try {
            taskNotificationService.sendIncompleteTasksNotification();
            String message = "Successfully sent incomplete task notifications to all employees";
            logger.info(message);
            return ResponseEntity.ok(message);
            
        } catch (Exception e) {
            String errorMessage = "Failed to send incomplete task notifications: " + e.getMessage();
            logger.error(errorMessage, e);
            return ResponseEntity.status(500).body(errorMessage);
        }
    }
}
