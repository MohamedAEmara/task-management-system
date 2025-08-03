package com.emara.task.repo;

import com.emara.task.model.Manager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ManagerRepository extends JpaRepository<Manager, Integer> {
    public Manager findByUserId(Integer userId);
}
