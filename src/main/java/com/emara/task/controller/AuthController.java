package com.emara.task.controller;

import com.emara.task.dto.LoginRequestDto;
import com.emara.task.dto.LoginResponseDto;
import com.emara.task.dto.VerifyAccountRequestDto;
import com.emara.task.security.JwtUtil;
import org.springframework.http.ResponseEntity;
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
    @Autowired
    private JwtUtil jwtUtil;


    @GetMapping("/ping") 
    public String ping() {
        System.out.println("Ping received");
        return "pong";
    }

    @PostMapping("/signup/employee")
    public User signup(@RequestBody SignupEmployeeDto employeeDto) {
        return userService.createEmployeeUser(employeeDto);
    }


    @PostMapping("/verify")
    public ResponseEntity<String> verify(@RequestBody VerifyAccountRequestDto request) {
        userService.verifyOtp(request.getUsername(), request.getOtp());
        return ResponseEntity.ok("User verified successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto request) {
        return userService.loginUser(request);
    }
}
