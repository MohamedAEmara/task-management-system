package com.emara.task.repo;

import com.emara.task.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Integer> {
    @Query("select t from Task t where t.assignedTo.user.id = :userId")
    public List<Task> getEmployeeTasks(Integer userId);

    @Query("select t from Task t where t.assignedFrom.user.id = :userId")
    public List<Task> getManagerTasks(Integer userId);
}
