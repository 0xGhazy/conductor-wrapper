package com.vodafone.vcs.conductorwrapper.web.middleware;

import com.vodafone.vcs.conductorwrapper.common.IDGenerator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Log4j2
@Component
public class AuthenticationAndTracingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        try {
            String traceId = IDGenerator.generateTraceId();
            MDC.put("traceId", traceId);
            log.debug("Attempting to execute {} request", request.getRequestURI());

            chain.doFilter(request, response);
        } finally {
            log.info("I am in finally filter");
            MDC.clear();
        }
    }
}
