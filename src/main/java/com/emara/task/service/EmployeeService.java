package com.emara.task.service;

import com.emara.task.model.Department;
import com.emara.task.model.Manager;
import com.emara.task.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;

import com.emara.task.model.Employee;
import com.emara.task.repo.EmployeeRepository;

@Service
public class EmployeeService {
    @Autowired
    private EmployeeRepository employeeRepository;
    
    public void addEmployee(Employee employee) {
        employeeRepository.save(employee);
    }

    @CacheEvict(value = "employees", key = "'userId:' + #userId")
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

    @Cacheable(value = "employees", key = "'userId:' + #userId")
    public Employee findByUserId(Integer userId) {
        System.out.println("Cache miss: Loading employee from database for userId: " + userId);
        return employeeRepository.findByUserId(userId);
    }

    @CachePut(value = "employees", key = "'userId:' + #employee.user.id")
    public Employee save(Employee employee) {
        System.out.println("Updating cache: Saving employee for userId: " + employee.getUser().getId());
        return employeeRepository.save(employee);
    }
}
