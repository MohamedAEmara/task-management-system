package com.emara.task.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "notification.enabled", havingValue = "true", matchIfMissing = true)
public class ScheduledJobService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledJobService.class);

    @Autowired
    private TaskNotificationService taskNotificationService;

    /**
     * Run every day at 9:00 PM
     * Cron format: second minute hour day month day-of-week
     * "0 0 9 * * ?" → 9:00:00 daily
     */
        @Scheduled(cron = "0 * * * * ?")
    public void sendDailyIncompleteTaskNotifications() {
        logger.info("Starting scheduled job: sendDailyIncompleteTaskNotifications");

        try {
            taskNotificationService.sendIncompleteTasksNotification();
            logger.info("Successfully completed scheduled job: sendDailyIncompleteTaskNotifications");
        } catch (Exception e) {
            logger.error("Failed to execute scheduled job: sendDailyIncompleteTaskNotifications", e);
        }
    }
}
