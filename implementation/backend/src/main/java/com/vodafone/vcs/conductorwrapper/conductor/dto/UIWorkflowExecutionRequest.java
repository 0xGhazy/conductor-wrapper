package com.vodafone.vcs.conductorwrapper.conductor.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
public class UIWorkflowExecutionRequest {
    @NotNull private Meta meta;
    @NotNull private List<Node> nodes;
    @NotNull private List<Edge> edges;
    @NotNull private List<String> order;
    @NotNull private List<List<String>> paths;
    @NotNull private List<Object> branchActions;

    @Override
    public String toString() {
        return "{" +
                "meta=" + meta +
                ", nodes=" + nodes +
                ", edges=" + edges +
                ", order=" + order +
                ", paths=" + paths +
                ", branchActions=" + branchActions +
                '}';
    }

    @Data
    public static class Meta {
        @NotNull private String workflowName;
        @NotNull private Instant exportedAt;
        @NotNull private Integer version;

        @Override
        public String toString() {
            return "{" +
                    "workflowName='" + workflowName + '\'' +
                    ", version=" + version +
                    ", exportedAt=" + exportedAt +
                    '}';
        }
    }

    @Data
    public static class Node {
        @NotNull private String id;
        @NotNull private String type;
        @NotNull private Position position;
        @JsonInclude(JsonInclude.Include.NON_NULL) private NodeData data;

        @Override
        public String toString() {
            return "{" +
                    "id='" + id + '\'' +
                    ", type='" + type + '\'' +
                    ", position=" + position +
                    ", data=" + data +
                    '}';
        }
    }

    @Data
    public static class Position {
        private double x;
        private double y;

        @Override
        public String toString() {
            return "{" +
                    "x=" + x +
                    ", y=" + y +
                    '}';
        }
    }

    @Data
    public static class NodeData {
        @JsonInclude(JsonInclude.Include.NON_NULL) private String name;
        @JsonInclude(JsonInclude.Include.NON_NULL) private String kind;
        @JsonInclude(JsonInclude.Include.NON_NULL) private Map<String, Object> config;

        @Override
        public String toString() {
            return "{" +
                    "name='" + name + '\'' +
                    ", kind='" + kind + '\'' +
                    ", config=" + config +
                    '}';
        }
    }

    @Data
    public static class Edge {
        @NotNull private String id;
        @NotNull private String source;
        @NotNull private String target;
        @JsonInclude(JsonInclude.Include.NON_NULL) private String sourceHandle;
        @JsonInclude(JsonInclude.Include.NON_NULL) private String targetHandle;

        @Override
        public String toString() {
            return "{" +
                    "id='" + id + '\'' +
                    ", source='" + source + '\'' +
                    ", target='" + target + '\'' +
                    ", sourceHandle='" + sourceHandle + '\'' +
                    ", targetHandle='" + targetHandle + '\'' +
                    '}';
        }
    }
}