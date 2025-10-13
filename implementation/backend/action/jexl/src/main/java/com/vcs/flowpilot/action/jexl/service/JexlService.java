package com.vcs.flowpilot.action.jexl.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.jexl3.*;
import org.apache.commons.jexl3.introspection.JexlPermissions;
import org.apache.commons.jexl3.introspection.JexlPermissions.ClassPermissions;
import org.apache.commons.jexl3.introspection.JexlSandbox;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class JexlService implements AutoCloseable{

    private final JexlEngine engine;
    @Setter @Getter
    private Map<String, Object> namespaces = new HashMap<>();
    private Set<String> permissions = new HashSet<>();
    private final ExecutorService executor = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "jexl-runner");
        t.setDaemon(true);
        return t;
    });

    public JexlService() {
        permissions.add("java.lang.Math");
        permissions.add("java.lang.StrictMath");

        ClassPermissions perm = new ClassPermissions(JexlPermissions.RESTRICTED, permissions);
        JexlSandbox sandbox = new JexlSandbox();
        namespaces.put("math", java.lang.StrictMath.class);

        this.engine = new JexlBuilder()
                .namespaces(namespaces)
                .permissions(perm)
                .sandbox(sandbox)
                .strict(true)
                .silent(false)
                .create();
    }

    public Object evaluate(String script, JexlContext ctx, java.time.Duration timeout) throws Exception {
        var exec = java.util.concurrent.Executors.newSingleThreadExecutor();
        try {
            var future = exec.submit(() -> engine.createScript(script).execute(ctx));
            return future.get(timeout.toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);
        } finally {
            exec.shutdownNow();
        }
    }

    public static JexlContext ctxOf(Map<String,Object> vars) {
        JexlContext ctx = new MapContext();
        vars.forEach(ctx::set);
        return ctx;
    }

    @Override
    public void close() {
        executor.shutdownNow();
    }

}
