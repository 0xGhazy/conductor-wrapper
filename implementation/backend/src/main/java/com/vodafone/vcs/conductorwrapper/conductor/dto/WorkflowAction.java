package com.vodafone.vcs.conductorwrapper.conductor.dto;


import com.vodafone.vcs.conductorwrapper.conductor.enums.ActionType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

import java.util.HashMap;

@Data
public class WorkflowAction {
    @Enumerated(EnumType.STRING)
    private ActionType type;
    private String name;
    private HashMap<String, Object> config;
}
