package com.emara.task.service;


import com.emara.task.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    public String generateOtp() {
        return String.format("%06d", (int) (Math.random() * 900000) + 100000); // 6-digit OTP
    }
}