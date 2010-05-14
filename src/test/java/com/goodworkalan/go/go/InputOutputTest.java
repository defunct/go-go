package com.goodworkalan.go.go;

import org.testng.annotations.Test;

/**
 * Unit tests for the {@link InputOutput} class.
 *
 * @author Alan Gutierrez
 */
public class InputOutputTest {
    /** Test missing encoding. */
    @Test(expectedExceptions = RuntimeException.class)
    public void invalidEncoding() {
        InputOutput.nulls("", "");
    }
    
    /** Test nulls. */
    @Test
    public void nulls() {
        InputOutput.nulls();
    }
}
