package com.conductor.core.conductor.metadata.adapter;

import com.conductor.core.conductor.metadata.dto.TaskDefDto;
import com.conductor.core.conductor.metadata.dto.TaskDto;
import com.netflix.conductor.common.metadata.tasks.TaskDef;
import com.netflix.conductor.common.metadata.workflow.WorkflowTask;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskAdapter {

    private final ModelMapper mapper;

    public TaskDef toTaskDef(TaskDefDto taskDefDto) {
        return mapper.map(taskDefDto, TaskDef.class);
    }

    public TaskDefDto toTaskDto(TaskDef def) {
        return mapper.map(def, TaskDefDto.class);
    }

    public WorkflowTask toTask(TaskDto dto) {
        return mapper.map(dto, WorkflowTask.class);
    }

}
