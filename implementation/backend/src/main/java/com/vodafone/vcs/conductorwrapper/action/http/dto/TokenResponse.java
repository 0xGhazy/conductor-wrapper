package com.vodafone.vcs.conductorwrapper.action.http.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NonNull;

public record TokenResponse (
        @JsonProperty("access_token")  String accessToken,
        @JsonProperty("refresh_token") String refreshToken,
        @JsonProperty("expires_in")    Long   expiresIn,
        @JsonProperty("token_type")    String tokenType
) {
    @NonNull @Override
    public String toString() {
        return "{" +
                "accessToken='" + accessToken + '\'' +
                ", refreshToken='" + refreshToken + '\'' +
                ", expiresIn=" + expiresIn +
                ", tokenType='" + tokenType + '\'' +
                '}';
    }
}
