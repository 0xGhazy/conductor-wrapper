package com.vodafone.vcs.conductorwrapper.action.database.dto;

import com.vodafone.vcs.conductorwrapper.action.database.enums.DatasourceStatus;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@ToString
public final class DatasourceState {
    private final String name;
    private volatile DatasourceStatus status;
    private volatile String lastError;
    private volatile LocalDateTime lastChecked;

    public DatasourceState(String name) {
        this.name = name; this.status = DatasourceStatus.ACTIVE;
        this.lastChecked = LocalDateTime.now();
    }
    public void ok() { this.status = DatasourceStatus.ACTIVE; this.lastError=null; this.lastChecked=LocalDateTime.now(); }
    public void down(String err){ this.status = DatasourceStatus.ERROR; this.lastError=err; this.lastChecked=LocalDateTime.now(); }
    public void inactive(){ this.status = DatasourceStatus.INACTIVE; this.lastChecked=LocalDateTime.now(); }
}