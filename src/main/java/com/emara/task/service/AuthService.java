package com.emara.task.service;

import org.springframework.stereotype.Service;

@Service
public class AuthService {
    public String generateOtp() {
        return String.format("%06d", (int) (Math.random() * 900000) + 100000); // 6-digit OTP
    }
}