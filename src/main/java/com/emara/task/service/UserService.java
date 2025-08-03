package com.emara.task.service;

import com.emara.task.dto.LoginRequestDto;
import com.emara.task.dto.LoginResponseDto;
import com.emara.task.dto.SignupEmployeeDto;
import com.emara.task.dto.VerifyAccountRequestDto;
import com.emara.task.model.*;
import com.emara.task.repo.UserRepository;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.emara.task.security.JwtUtil;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ManagerService managerService;

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
        user.setRole(Role.EMPLOYEE);
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
        employeeService.addEmployee(emp);

        return user;
    }

    public void verifyOtp(String username, String otp) {
        if(username == null || otp == null) {

        }
        Optional<User> user = userRepository
                .findByUsername(username);
        if(!user.isPresent()) {
            throw new RuntimeException("User not found!");
        }
        String redisKey = "verify-" + user.get().getEmail();
        String redisOtp = redisTemplate.opsForValue().getAndDelete(redisKey);

        if(redisOtp.equals(otp)) {
            user.get().setVerified(true);
            userRepository.save(user.get());
        } else {
            throw new RuntimeException("Wrong OPT");
        }
    }

    public ResponseEntity<LoginResponseDto> loginUser(LoginRequestDto request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
            UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());

            String token = jwtUtil.generateToken(userDetails);
            return ResponseEntity.ok(new LoginResponseDto("success", 200, token));
        } catch (BadCredentialsException e) {
            System.out.println("Bad credentials: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new LoginResponseDto("Invalid username or password", 401, null));
        } catch (DisabledException e) {
            System.out.println("User not verified: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new LoginResponseDto("User not verified", 401, null));
        } catch (Exception e) {
            System.out.println("Authentication error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new LoginResponseDto("Login failed: " + e.getMessage(), 500, null));
        }
    }

    public ResponseEntity<?> resendOtp(VerifyAccountRequestDto request) {
        try {
            System.out.println("Requssssssssssssssst");
            System.out.println(request);
            if (request.getUsername() == null) {
                throw new BadRequestException("Username not found!");
            }
            User user = userRepository.findByUsername(request.getUsername()).orElseThrow(
                    () -> {
                        throw new UsernameNotFoundException("User not found!");
                    }
            );

            String otp = authService.generateOtp();
            String redisKey = "verify-" + user.getEmail();
            redisTemplate.opsForValue().set(redisKey, otp, 10, TimeUnit.MINUTES);

            // Send email with OTP
            MailStructure mailStructure = new MailStructure();
            mailStructure.setSubject("Email Verification");
            mailStructure.setMessage("Your OTP for email verification is: " + otp);
            mailService.sendMail(user.getEmail(), mailStructure);

            return ResponseEntity.ok("Verification resent successfully!");
        } catch (Exception ex) {
            System.out.println("Error in resending verification: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new LoginResponseDto("Login failed: " + ex.getMessage(), 500, null));
        }
    }


    public ResponseEntity<?> promoteUser(VerifyAccountRequestDto request) {
        try {
            System.out.println("Requssssssssssssssst");
            System.out.println(request);
            if (request.getUsername() == null) {
                throw new BadRequestException("Username not found!");
            }
            User user = userRepository.findByUsername(request.getUsername()).orElseThrow(
                    () -> {
                        throw new UsernameNotFoundException("User not found!");
                    }
            );
            // Delete employee entity for this user if exists
            if(user.getRole() == Role.EMPLOYEE) {
                employeeService.deleteEmployeeEntity(user.getId());
            }

            // Change role field for this user to "MANAGER"
            user.setRole(Role.MANAGER);

            // Save updates to DB
            userRepository.save(user);

            // Create manager entity for this user.
            Manager manager = managerService.createManagerEntity(user);

            return ResponseEntity.ok("User upgraded successfully" + manager.toString());
        } catch (Exception ex) {
            System.out.println("Error in promoting user: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new LoginResponseDto("Login failed: " + ex.getMessage(), 500, null));
        }
    }
}
