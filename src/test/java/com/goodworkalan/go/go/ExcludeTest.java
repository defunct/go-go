package com.goodworkalan.go.go;

import static com.goodworkalan.go.go.GoException.INVALID_EXCLUDE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

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
    @Test(expectedExceptions = GoException.class)
    public void badExclude() {
        exceptional(INVALID_EXCLUDE, new Runnable() {
            public void run() {
                new Exclude("ant/ant/6.1");
            }
        });
    }

    /**
     * Run the given code block, catch the expected <code>GoException</code> and
     * check that the error code matches the given error code.
     * 
     * @param code
     *            The error code.
     * @param runnable
     *            The exceptional code block.
     * @exception GoException
     *                If all goes accordingly.
     */
    public static void exceptional(int code, Runnable runnable) {
        try {
            runnable.run();
        } catch (GoException e) {
            assertEquals(e.getCode(), code);
            if (Integer.toString(e.getCode()).equals(e.getMessage())) {
                fail("No message for error code: " + e.getCode());
            }
            throw e;
        }

    }
}
