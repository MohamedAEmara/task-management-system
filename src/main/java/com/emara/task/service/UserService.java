package com.emara.task.service;

import com.emara.task.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    // @Autowired
    // private PasswordEncoder
}
