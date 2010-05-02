package com.goodworkalan.go.go;
import java.io.File;
import java.util.Collections;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Unit tests for the {@link ProgramQueue} class.
 *
 * @author Alan Gutierrez
 */
public class ProgramQueueTest {
    /** Run a simple program queue. */
    @Test
    public void run() {
        assertEquals(new ProgramQueue(Collections.singletonList(new File(System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository")), "boot", "hello").start(), 0);
    }
}
