package com.vcs.conductor.metadata.adapter;

import com.netflix.conductor.common.metadata.workflow.WorkflowDef;
import com.vcs.conductor.metadata.dto.WorkflowDefDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class WorkflowAdapter {

    private final ModelMapper mapper;

    public WorkflowDef toDefinition(WorkflowDefDto dto) {
        return mapper.map(dto, WorkflowDef.class);
    }

    public WorkflowDefDto toDto(WorkflowDef def) {
        return mapper.map(def, WorkflowDefDto.class);
    }
}
