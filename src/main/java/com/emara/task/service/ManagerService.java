package com.emara.task.service;

import com.emara.task.model.Employee;
import com.emara.task.model.Manager;
import com.emara.task.model.User;
import com.emara.task.repo.ManagerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

@Service
public class ManagerService {
    @Autowired
    private ManagerRepository managerRepository;

    public Manager createManagerEntity(User user) {
        Manager manager = new Manager();
//        manager.setUserId(user.getId());
        manager.setUser(user);
        managerRepository.save(manager);
        return manager;
    }

    @CacheEvict(value = "managers", key = "'userId:' + #userId")
    public void deleteManagerEntity(Integer userId) {
        Manager manager = managerRepository.findByUserId(userId);
        managerRepository.deleteById(manager.getId());
    }

    @Cacheable(value = "managers", key = "'userId:' + #userId")
    public Manager findManagerByUserId(Integer userId) {
        System.out.println("Cache miss: Loading manager from database for userId: " + userId);
        Manager manager = managerRepository.findByUserId(userId);
        return manager;
    }
}
