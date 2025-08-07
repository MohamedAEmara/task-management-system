package com.emara.task.dto;

import com.emara.task.model.TaskStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskStatusDto {
    private Integer taskId;
    private TaskStatus status;
}
