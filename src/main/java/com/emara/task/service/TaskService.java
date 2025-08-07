package com.emara.task.service;

import com.emara.task.dto.TaskDto;
import com.emara.task.model.Manager;
import com.emara.task.model.Task;
import com.emara.task.model.TaskStatus;
import com.emara.task.model.User;
import com.emara.task.repo.TaskRepository;
import com.emara.task.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TaskService {
    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ManagerService managerService;

    @Autowired
    private UserService userService;

    public ResponseEntity<?> createTask(TaskDto taskDto) {
       try {
           UserDetails userDetails = jwtUtil.getUser();
           User user = userService.findByUsername(userDetails.getUsername());
           Task task = new Task();
           Manager manager = managerService.findManagerByUserId(user.getId());
           task.setTitle(taskDto.getTitle());
           task.setDescription(taskDto.getDescription());
           task.setStatus(TaskStatus.PENDING);
           task.setCreatedAt(LocalDateTime.now());
           task.setUpdatedAt(LocalDateTime.now());
           task.setAssignedFrom(manager);

           taskRepository.save(task);
           return ResponseEntity.ok(task);
       } catch (Exception ex) {
           System.out.println(ex.getMessage());
           return ResponseEntity.status(500).body("Error creating new task: " + ex.getMessage());
       }

    }
}
