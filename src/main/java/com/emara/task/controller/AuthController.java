package com.emara.task.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.emara.task.dto.SignupEmployeeDto;
import com.emara.task.model.User;
import com.emara.task.service.UserService;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/auth")
public class AuthController {
    @PostConstruct
    public void init() {
        System.out.println("AuthController loaded successfully!");
    }
    @Autowired
    private UserService userService;

    @GetMapping("/ping") 
    public String ping() {
        System.out.println("Ping received");
        return "pong";
    }

    @PostMapping("/signup/employee")
    public User signup(@RequestBody SignupEmployeeDto employeeDto) {
        return userService.createEmployeeUser(employeeDto);
    }
}
