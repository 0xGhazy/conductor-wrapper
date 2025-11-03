package com.vodafone.vcs.conductor.action.database.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Getter
@Log4j2
@Builder
@ToString
@Component
public class Query {

    private String id;
    @Builder.Default
    private Map<String, Object> params = new HashMap<>();

}

