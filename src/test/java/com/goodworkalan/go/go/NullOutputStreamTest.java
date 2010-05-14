package com.goodworkalan.go.go;

import java.io.IOException;

import org.testng.annotations.Test;

/**
 * Unit tests for the {@link NullOutputStream] class.
 *
 * @author Alan Gutierrez
 */
public class NullOutputStreamTest {
    /** Test the null output stream. */
    @Test
    public void everything() throws IOException {
        NullOutputStream.nullStream().write(0);
    }
}
