package com.vcs.flowpilot.action.jexl;

import com.vcs.flowpilot.action.jexl.service.JexlService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JexlApplicationTests {

    private JexlService runner = new JexlService();

    @AfterEach
    void tearDown() throws Exception {
        runner.close();
    }

    @Test
    void allowedMath_shouldReturnResult() throws Exception {
        Object result = runner.evaluate("math:max(2, 5) + 1", JexlService.ctxOf(Map.of()), Duration.ofSeconds(2));
        assertEquals(6, ((Number) result).intValue());
    }

    @Test
    void insecureExample_printsProcessOutput() throws Exception {
        String script = "java.lang.Runtime.getRuntime().exec('whoami')";
        String obj = (String) runner.evaluate(script, JexlService.ctxOf(Map.of()), java.time.Duration.ofSeconds(1));
        System.out.println(obj);
        assertNull(obj);
    }

    @Test
    void infiniteLoop_shouldTimeout() {
        String script = "var i = 0; while (true) { i = i + 1; }";
        assertThrows(Exception.class, () -> runner.evaluate(script, JexlService.ctxOf(Map.of()), Duration.ofMillis(300)));
    }

}
