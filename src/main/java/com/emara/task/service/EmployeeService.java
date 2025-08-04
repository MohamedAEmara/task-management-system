package com.emara.task.service;

import com.emara.task.model.Manager;
import com.emara.task.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.emara.task.model.Employee;
import com.emara.task.repo.EmployeeRepository;

@Service
public class EmployeeService {
    @Autowired
    private EmployeeRepository employeeRepository;
    
    public void addEmployee(Employee employee) {
        employeeRepository.save(employee);
    }

    public void deleteEmployeeEntity(Integer userId) {
        Employee employee = employeeRepository.findByUserId(userId);
        employeeRepository.deleteById(employee.getId());
    }

    public Employee createEmployeeEntity(User user) {
        Employee employee = new Employee();
        employee.setUser(user);
        employeeRepository.save(employee);
        return employee;
    }
}
