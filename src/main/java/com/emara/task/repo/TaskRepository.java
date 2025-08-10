package com.emara.task.repo;

import com.emara.task.model.Task;
import com.emara.task.model.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Integer> {
    @Query("select t from Task t where t.assignedTo.user.id = :userId")
    public List<Task> getEmployeeTasks(Integer userId);

    @Query("select t from Task t where t.assignedFrom.user.id = :userId")
    public List<Task> getManagerTasks(Integer userId);
    
    @Query("select t from Task t where t.assignedTo.user.id = :userId and t.status != :completedStatus")
    public List<Task> getEmployeeIncompleteTasks(@Param("userId") Integer userId, @Param("completedStatus") TaskStatus completedStatus);
    
    @Query("select count(t) from Task t where t.assignedTo.user.id = :userId and t.status != :completedStatus")
    public Long countEmployeeIncompleteTasks(@Param("userId") Integer userId, @Param("completedStatus") TaskStatus completedStatus);

    // Employee Tasks:
    Page<Task> findByAssignedTo_User_Id(Integer userId, Pageable pageable);
    Page<Task> findByAssignedTo_User_IdAndStatus(Integer userId, TaskStatus status, Pageable pageable);

    // Manager tasks
    Page<Task> findByAssignedFrom_User_Id(Integer userId, Pageable pageable);
    Page<Task> findByAssignedFrom_User_IdAndStatus(Integer userId, TaskStatus status, Pageable pageable);
}
