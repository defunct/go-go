package com.goodworkalan.go.go;

import static com.goodworkalan.go.go.GoException.INVALID_EXCLUDE;

import org.testng.annotations.Test;

/**
 * Unit tests for the {@link Include} class.
 *
 * @author Alan Gutierrez
 */
public class IncludeTest {
    /** Test a bad exclude string. */
    @Test
    public void badExclude() {
        new GoExceptionCatcher(INVALID_EXCLUDE, new Runnable() {
            public void run() {
                Include.exclude("ant/ant/6.1");
            }
        }).run();
    }
}
