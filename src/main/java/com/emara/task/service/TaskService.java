package com.emara.task.service;

import com.emara.task.dto.AssignTaskDto;
import com.emara.task.dto.TaskDto;
import com.emara.task.dto.TaskStatusDto;
import com.emara.task.model.*;
import com.emara.task.repo.TaskRepository;
import com.emara.task.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
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

    @Autowired
    private EmployeeService employeeService;

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

    public ResponseEntity<?> getMyTasks(int page, int size, TaskStatus status) {
        try {
            UserDetails userDetails = jwtUtil.getUser();
            User user = userService.findByUsername(userDetails.getUsername());

            Pageable pageable = PageRequest.of(page, size);
            Page<Task> tasks;

            if(user.getRole() == Role.EMPLOYEE) {
                if (status != null) {
                    tasks = taskRepository.findByAssignedTo_User_IdAndStatus(user.getId(), status, pageable);
                } else {
                    tasks = taskRepository.findByAssignedTo_User_Id(user.getId(), pageable);
                }
            } else if(user.getRole() == Role.MANAGER) {
                if (status != null) {
                    tasks = taskRepository.findByAssignedFrom_User_IdAndStatus(user.getId(), status, pageable);
                } else {
                    tasks = taskRepository.findByAssignedFrom_User_Id(user.getId(), pageable);
                }
            } else {
                return ResponseEntity.badRequest().body("Invalid Role!");
            }

            return ResponseEntity.ok(tasks);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return ResponseEntity.status(500).body("Error fetching user tasks: " + ex.getMessage());
        }
    }

    public ResponseEntity<?> assignTaskToEmployee(AssignTaskDto assignTaskDto) {
        try {
            if(assignTaskDto.getTaskId() == null || assignTaskDto.getUserId() == null) {
                throw new Exception("Missing fields taskId or userId!");
            }
            User user = userService.findByUsername(jwtUtil.getUser().getUsername());
            Manager manager = managerService.findManagerByUserId(user.getId());
            // Verify task belong to this Manager
            Task task = taskRepository.findById(assignTaskDto.getTaskId()).orElseThrow();
            // Verify employee belong to a department of this manager
            Employee employee = employeeService.findByUserId(assignTaskDto.getUserId());
            if(employee == null) {
                throw new Exception("Employee not found!");
            }
            if(employee.getDepartment().getManager() != manager) {
                throw new Exception("You are not a manager for this employee!");
            }

            // Assign task to this employee
            task.setAssignedTo(employee);
            taskRepository.save(task);
            return ResponseEntity.ok(task);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return ResponseEntity.status(500).body("Error assigning the task to the employee: " + ex.getMessage());
        }
    }

    public ResponseEntity<?> updateStatus(TaskStatusDto taskStatusDto) {
        try {
            if(taskStatusDto.getTaskId() == null || taskStatusDto.getStatus() == null) {
                throw new Exception("Missing fields taskId or status!");
            }
            User user = userService.findByUsername(jwtUtil.getUser().getUsername());
            Employee employee = employeeService.findByUserId(user.getId());
            Task task = taskRepository.findById(taskStatusDto.getTaskId()).orElseThrow();
            if(employee == null) {
                throw new Exception("User not found!");
            }

            if(task.getAssignedTo() != employee) {
                throw new Exception("Wrong task ID");
            }

            if(
                taskStatusDto.getStatus() != TaskStatus.PENDING
                && taskStatusDto.getStatus() != TaskStatus.IN_PROGRESS
                && taskStatusDto.getStatus() != TaskStatus.COMPLETED
            ) {
                throw new Exception("Status must be one of the following: {PENDING, IN_PROGRESS, COMPLETED}");
            }

            task.setStatus(taskStatusDto.getStatus());
            taskRepository.save(task);

            return ResponseEntity.ok(task);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return ResponseEntity.status(500).body("Error updating task status: " + ex.getMessage());
        }
    }
}
