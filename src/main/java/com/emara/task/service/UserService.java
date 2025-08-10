package com.emara.task.service;

import com.emara.task.dto.*;
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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;


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

    @Cacheable(value = "users", key = "'username:' + #username")
    public User findByUsername(String username) {
        System.out.println("Cache miss: Loading user from database for username: " + username);
        return userRepository.findByUsername(username).orElseThrow();
    }
    
    @Cacheable(value = "users", key = "'email:' + #email")
    public User findByEmail(String email) {
        System.out.println("Cache miss: Loading user from database for email: " + email);
        return userRepository.findByEmail(email).orElseThrow();
    }
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
            // Evict cache for this user
            evictUserCacheByUsername(user.get().getUsername());
            evictUserCacheByEmail(user.get().getEmail());
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new LoginResponseDto("Login failed: " + ex.getMessage(), 500, null));
        }
    }


    public ResponseEntity<?> promoteUser(VerifyAccountRequestDto request) {
        try {
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
            if (user.getRole() == Role.MANAGER) {
                managerService.deleteManagerEntity(user.getId());
            }
            // Change role field for this user to "MANAGER"
            user.setRole(Role.MANAGER);

            // Save updates to DB
            userRepository.save(user);
            // Evict cache for this user
            evictUserCacheByUsername(user.getUsername());
            evictUserCacheByEmail(user.getEmail());

            // Create manager entity for this user.
            Manager manager = managerService.createManagerEntity(user);

            return ResponseEntity.ok("User upgraded successfully" + manager.toString());
        } catch (Exception ex) {
            System.out.println("Error in promoting user: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new LoginResponseDto("Login failed: " + ex.getMessage(), 500, null));
        }
    }

    public ResponseEntity<?> demoteUser(VerifyAccountRequestDto request) {
        try {
            if (request.getUsername() == null) {
                throw new BadRequestException("Username not found!");
            }
            User user = userRepository.findByUsername(request.getUsername()).orElseThrow(
                    () -> {
                        throw new UsernameNotFoundException("User not found!");
                    }
            );

            // Delete manager entity for this user if exists
            if (user.getRole() == Role.MANAGER) {
                managerService.deleteManagerEntity(user.getId());
            }
            if(user.getRole() == Role.EMPLOYEE) {
                employeeService.deleteEmployeeEntity(user.getId());
            }

            // Change role field for this user to "EMPLOYEE"
            user.setRole(Role.EMPLOYEE);

            // Save updates to DB
            userRepository.save(user);
            // Evict cache for this user
            evictUserCacheByUsername(user.getUsername());
            evictUserCacheByEmail(user.getEmail());

            // Create employee entity for this user.
            Employee employee = employeeService.createEmployeeEntity(user);

            return ResponseEntity.ok("User downgraded successfully" + employee.toString());
        } catch (Exception ex) {
            System.out.println("Error in demoting user: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new LoginResponseDto("Login failed: " + ex.getMessage(), 500, null));
        }
    }

    public ResponseEntity<?> forgotPassword (VerifyAccountRequestDto dto) {
        try {
            if(dto.getUsername() == null) {
                throw new Exception("Username not found!");
            }
            Optional<User> userOpt = userRepository.findByUsername(dto.getUsername());
            if(userOpt.isEmpty()) {
                throw new Exception("User not found!");
            }
            String reset = authService.generateOtp();
            String redisKey = "reset-" + userOpt.get().getEmail();
            redisTemplate.opsForValue().set(redisKey, reset, 10, TimeUnit.MINUTES);

            // Send email with OTP
            MailStructure mailStructure = new MailStructure();
            mailStructure.setSubject("Email Verification");
            mailStructure.setMessage("Your OTP for reset password: " + reset);
            mailService.sendMail(userOpt.get().getEmail(), mailStructure);
            return ResponseEntity.ok("OTP for password reset sent successfully!");
        } catch (Exception ex) {
            System.out.println("Error resetting password: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new LoginResponseDto("Failed to resent verify password OTP: " + ex.getMessage(), 500, null));
        }
    }

    public ResponseEntity<?> updatePassword(UpdatePasswordDto updatePasswordDto) {
        try {
            if(
                updatePasswordDto.getUsername() == null ||
                updatePasswordDto.getNewPassword() == null ||
                updatePasswordDto.getOtp() == null
            ) {
                throw new Exception("Missing required fields {username, newPassword, otp}");
            }
            Optional<User> userOpt = userRepository.findByUsername(updatePasswordDto.getUsername());
            if(userOpt.isEmpty()) {
                throw new Exception("User not found!");
            }
            User user = userOpt.get();
            String redisKey = "reset-" + user.getEmail();
            String redisOtp = redisTemplate.opsForValue().getAndDelete(redisKey);

            if(redisOtp.equals(updatePasswordDto.getOtp())) {
                user.setVerified(true);
                userRepository.save(user);
            } else {
                throw new RuntimeException("Wrong OPT");
            }
            user.setPassword(updatePasswordDto.getNewPassword());
            userRepository.save(user);
            // Evict cache for this user
            evictUserCacheByUsername(user.getUsername());
            evictUserCacheByEmail(user.getEmail());
            return ResponseEntity.ok("User password updated successfully");
        } catch (Exception ex) {
            System.out.println("Error resetting password: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new LoginResponseDto("Failed to updated password: " + ex.getMessage(), 500, null));
        }
    }
    
    // Helper method to evict user caches
    @CacheEvict(value = "users", allEntries = false)
    private void evictUserCaches(String username, String email) {
        // This will be called to evict specific user cache entries
        System.out.println("Evicting cache for user: " + username);
    }
    
    @CacheEvict(value = "users", key = "'username:' + #username")
    public void evictUserCacheByUsername(String username) {
        System.out.println("Evicted cache for username: " + username);
    }
    
    @CacheEvict(value = "users", key = "'email:' + #email")
    public void evictUserCacheByEmail(String email) {
        System.out.println("Evicted cache for email: " + email);
    }
}
