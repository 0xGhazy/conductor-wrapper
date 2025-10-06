package com.vcs.flowpilot.action.http.dto;

import com.vcs.flowpilot.action.http.security.contract.AuthStrategy;
import lombok.Data;

@Data
public class AuthConnection {
    private AuthStrategy strategy;
    private String connection;
    public final long expiresAtEpochSec;

    public AuthConnection(AuthStrategy s, long expEpochSec) { this.strategy = s; this.expiresAtEpochSec = expEpochSec; }

}
