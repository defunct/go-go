package com.goodworkalan.go.go;

import static com.goodworkalan.go.go.GoException.INVALID_EXCLUDE;

import org.testng.annotations.Test;

import com.goodworkalan.go.go.library.Exclude;
import com.goodworkalan.go.go.library.Include;

/**
 * Unit tests for the {@link Include} class.
 *
 * @author Alan Gutierrez
 */
public class ExcludeTest {
    /** Test a bad exclude string. */
    @Test
    public void badExclude() {
        new GoExceptionCatcher(INVALID_EXCLUDE, new Runnable() {
            public void run() {
                new Exclude("ant/ant/6.1");
            }
        }).run();
    }
}
