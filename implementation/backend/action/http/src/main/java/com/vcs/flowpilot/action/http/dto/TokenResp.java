package com.vcs.flowpilot.action.http.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TokenResp (
        @JsonProperty("access_token")  String accessToken,
        @JsonProperty("refresh_token") String refreshToken,
        @JsonProperty("expires_in")    Long   expiresIn,
        @JsonProperty("token_type")    String tokenType
) {
    @Override
    public String toString() {
        return "TokenResp{" +
                "accessToken='" + accessToken + '\'' +
                ", refreshToken='" + refreshToken + '\'' +
                ", expiresIn=" + expiresIn +
                ", tokenType='" + tokenType + '\'' +
                '}';
    }
}
