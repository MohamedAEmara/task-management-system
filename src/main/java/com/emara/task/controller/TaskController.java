package com.emara.task.controller;

import com.emara.task.dto.AssignTaskDto;
import com.emara.task.dto.TaskDto;
import com.emara.task.dto.TaskStatusDto;
import com.emara.task.model.Task;
import com.emara.task.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/task")
public class TaskController {
    @Autowired
    private TaskService taskService;

    @PostMapping
    public ResponseEntity<?> createTask(@RequestBody TaskDto taskDto) {
        return taskService.createTask(taskDto);
    }

    @GetMapping
    public ResponseEntity<?> getMyTasks() {
        return taskService.getMyTasks();
    }

    @PostMapping("/assign")
    public ResponseEntity<?> assignTaskToEmployee(@RequestBody AssignTaskDto assignTaskDto) {
        return taskService.assignTaskToEmployee(assignTaskDto);
    }

    @PatchMapping
    public ResponseEntity<?> updateStatus(@RequestBody TaskStatusDto taskStatusDto) {
        System.out.println("PATCH /task called with taskId: " + taskStatusDto.getTaskId() + ", status: " + taskStatusDto.getStatus());
        return taskService.updateStatus(taskStatusDto);
    }
}
