package com.vodafone.vcs.conductorwrapper.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import org.slf4j.MDC;

import java.io.Serializable;
import java.time.Instant;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseError implements Serializable {
    private String message;
    private Object reason;
    @Builder.Default
    private String requestId = MDC.get("requestId");
    @Builder.Default
    private Instant timestamp = Instant.now();
    private Object details;

    @Override
    public String toString() {
        return "{" +
                "message='" + message + '\'' +
                ", reason=" + reason +
                ", requestId='" + requestId + '\'' +
                ", timestamp=" + timestamp +
                ", details=" + details +
                '}';
    }

}
