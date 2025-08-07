package com.emara.task.service;

import com.emara.task.dto.TaskDto;
import com.emara.task.model.*;
import com.emara.task.repo.TaskRepository;
import com.emara.task.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    public ResponseEntity<?> getMyTasks() {
        try {
            UserDetails userDetails = jwtUtil.getUser();
            User user = userService.findByUsername(userDetails.getUsername());
            List<Task> tasks = new ArrayList<>();
            if(user.getRole() == Role.EMPLOYEE) {
                tasks = taskRepository.getEmployeeTasks(user.getId());
            } else if(user.getRole() == Role.MANAGER) {
                tasks = taskRepository.getManagerTasks(user.getId());
            }
            return ResponseEntity.ok(tasks);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return ResponseEntity.status(500).body("Error fetching user tasks: " + ex.getMessage());
        }
    }
}
