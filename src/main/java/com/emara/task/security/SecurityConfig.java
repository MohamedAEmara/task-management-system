package com.emara.task.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/auth/**").permitAll()
                    .requestMatchers("/mail/**").permitAll() // Allow mail endpoints for testing
                    .requestMatchers("/department/add").hasRole("MANAGER")
                    .requestMatchers("/department/remove").hasRole("MANAGER")
                    .requestMatchers(HttpMethod.GET, "/department").permitAll()
                    .requestMatchers(HttpMethod.POST, "/department").hasRole("ADMIN")
                    .requestMatchers("/department/me").hasAnyRole("EMPLOYEE", "MANAGER")
                    .requestMatchers("/my-department", "/my-manager").hasRole("EMPLOYEE")
                    .requestMatchers("/my-employees").hasRole("MANAGER")
                    .requestMatchers("/user/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/task").hasRole("MANAGER")
                    .requestMatchers(HttpMethod.GET, "/task").hasAnyRole("MANAGER", "EMPLOYEE")
                    .requestMatchers("/task/assign").hasRole("MANAGER")
                    .requestMatchers(HttpMethod.PATCH,"/task").hasRole("EMPLOYEE")
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

            return http.build();
        }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
