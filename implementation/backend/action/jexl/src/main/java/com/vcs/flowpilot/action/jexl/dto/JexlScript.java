package com.vcs.flowpilot.action.jexl.dto;

import lombok.Builder;
@Builder
public record JexlScript (String script, boolean cachable, int timeout) {

    @Override
    public String toString() {
        return "JexlScript{" +
                "script='" + script + '\'' +
                ", cachable=" + cachable +
                ", timeout=" + timeout +
                '}';
    }
}
