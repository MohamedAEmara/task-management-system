package com.emara.task.service;

import com.emara.task.config.RabbitMQConfig;
import com.emara.task.dto.MessageDto;
import com.emara.task.model.Employee;
import com.emara.task.model.MailStructure;
import com.emara.task.model.Task;
import com.emara.task.model.TaskStatus;
import com.emara.task.repo.EmployeeRepository;
import com.emara.task.repo.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskNotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(TaskNotificationService.class);
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private MailService mailService;

    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    public void sendIncompleteTasksNotification() {
        logger.info("Starting to send incomplete task notifications to all employees");
        
        List<Employee> allEmployees = employeeRepository.findAll();
        logger.info("Found {} employees to notify", allEmployees.size());
        
        for (Employee employee : allEmployees) {
            try {
                sendIncompleteTasksNotificationToEmployee(employee);
            } catch (Exception e) {
                logger.error("Failed to send notification to employee: {} ({})", 
                    employee.getUser().getUsername(), employee.getUser().getEmail(), e);
            }
        }
        
        logger.info("Completed sending incomplete task notifications");
    }
    
    private void sendIncompleteTasksNotificationToEmployee(Employee employee) {
        Integer userId = employee.getUser().getId();
        String userEmail = employee.getUser().getEmail();
        String userName = employee.getUser().getUsername();
        
        // Get count of incomplete tasks
        Long incompleteTasksCount = taskRepository.countEmployeeIncompleteTasks(userId, TaskStatus.COMPLETED);
        
        if (incompleteTasksCount == 0) {
            logger.info("Employee {} has no incomplete tasks, skipping notification", userName);
            return;
        }
        
        // Get the actual incomplete tasks for details
        List<Task> incompleteTasks = taskRepository.getEmployeeIncompleteTasks(userId, TaskStatus.COMPLETED);
        
        // Create email content
        MailStructure mailStructure = new MailStructure();
        mailStructure.setSubject("Task Reminder: You have " + incompleteTasksCount + " incomplete task(s)");
        
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("Dear ").append(userName).append(",\n\n");
        messageBuilder.append("This is a reminder that you currently have ").append(incompleteTasksCount).append(" incomplete task(s):\n\n");
        
        for (Task task : incompleteTasks) {
            messageBuilder.append("• Task: ").append(task.getTitle()).append("\n");
            messageBuilder.append("  Status: ").append(task.getStatus().name()).append("\n");
            messageBuilder.append("  Description: ").append(task.getDescription() != null ? task.getDescription() : "No description").append("\n");
            messageBuilder.append("  Created: ").append(task.getCreatedAt().toLocalDate()).append("\n\n");
        }
        
        messageBuilder.append("Please review and update your tasks as needed.\n\n");
        messageBuilder.append("Best regards,\n");
        messageBuilder.append("Task Management System");
        
        mailStructure.setMessage(messageBuilder.toString());
        
//        // Send the email
//        mailService.sendMail(userEmail, mailStructure);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EMAIL_QUEUE, new MessageDto(userEmail, mailStructure.getSubject(), mailStructure.getMessage()));
        logger.info("Sent incomplete tasks notification to {} ({}): {} tasks", userName, userEmail, incompleteTasksCount);
    }
}
