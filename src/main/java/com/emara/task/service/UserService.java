package com.emara.task.service;

import com.emara.task.dto.SignupEmployeeDto;
import com.emara.task.model.Employee;
import com.emara.task.model.MailStructure;
import com.emara.task.model.User;
import com.emara.task.repo.UserRepository;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MailService mailService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private AuthService authService;

    @Autowired
    private EmployeeService employeeService;

    public User createEmployeeUser(SignupEmployeeDto employeeDto) {
        if (
            userRepository.findByUsername(employeeDto.getUsername()).isPresent() || 
            userRepository.findByEmail(employeeDto.getEmail()).isPresent()
        ) {
            throw new RuntimeException("Username or Email already exists");
        }
        User user = new User();
        user.setUsername(employeeDto.getUsername());
        user.setEmail(employeeDto.getEmail());
        user.setPassword(passwordEncoder.encode(employeeDto.getPassword()));

        String otp = authService.generateOtp();
        String redisKey = "verify-" + employeeDto.getEmail();
        redisTemplate.opsForValue().set(redisKey, otp, 10, TimeUnit.MINUTES);

        // Send email with OTP
        MailStructure mailStructure = new MailStructure();
        mailStructure.setSubject("Email Verification");
        mailStructure.setMessage("Your OTP for email verification is: " + otp);
        mailService.sendMail(employeeDto.getEmail(), mailStructure);

        User savedUser = userRepository.save(user);
        System.out.println("User created with ID: " + savedUser.getId());
        System.out.println(savedUser);

        // Create Employee entity 
        Employee emp = new Employee();
        emp.setUser(user);
        // emp.setUserId(user.getId());
        employeeService.addEmployee(emp);

        return user;
    }
}
