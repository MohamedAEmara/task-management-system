package com.emara.task.controller;

import com.emara.task.dto.VerifyAccountRequestDto;
import com.emara.task.service.UserService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping
    public String testUserEndpoint() {
        return "/user endpoint is working";
    }

    @PostMapping
    public ResponseEntity<?> promoteUserToManager(@RequestBody VerifyAccountRequestDto requestDto) {
        return userService.promoteUser(requestDto);
    }
}
