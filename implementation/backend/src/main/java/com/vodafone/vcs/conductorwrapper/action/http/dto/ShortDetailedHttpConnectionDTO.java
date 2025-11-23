package com.vodafone.vcs.conductorwrapper.action.http.dto;

import com.vodafone.vcs.conductorwrapper.action.http.enums.AuthenticationStrategy;
import com.vodafone.vcs.conductorwrapper.common.contract.AuthStrategy;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ShortDetailedHttpConnectionDTO {
    private UUID id;
    private String name;
    private AuthenticationStrategy strategy;
    private boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
}
