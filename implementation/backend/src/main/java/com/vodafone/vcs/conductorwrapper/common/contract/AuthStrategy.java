package com.vodafone.vcs.conductorwrapper.common.contract;

import org.springframework.web.reactive.function.client.WebClient;

public interface AuthStrategy {
    WebClient.RequestHeadersSpec<?> apply( WebClient.RequestHeadersSpec<?> req);
    boolean testAuthentication();
}
