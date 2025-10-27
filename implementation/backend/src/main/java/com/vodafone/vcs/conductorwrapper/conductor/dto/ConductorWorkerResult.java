package com.vodafone.vcs.conductorwrapper.conductor.dto;

import com.vodafone.vcs.conductorwrapper.common.contract.WorkerResult;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConductorWorkerResult {
    private String traceId;
    private long timestamp;
    private WorkerResult result;
}
