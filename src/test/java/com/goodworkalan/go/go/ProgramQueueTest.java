package com.goodworkalan.go.go;

import java.io.File;
import java.util.Collections;
import java.util.List;

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
        assertEquals(new ProgramQueue(Collections.singletonList(new File(System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository")), "--verbose", "boot", "hello").run(new InputOutput()), 0);
    }

    /**
     * Get the default <code>~/.m2/repository</code> library.
     * 
     * @return The library path containing only the default library.
     */
    private List<File> getLibrary() {
        return Collections.singletonList(new File(System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository"));
    }

    /** Call another command from the executor. */
    @Test
    public void runButton() {
        assertEquals(new ProgramQueue(getLibrary(), "snap", "--mississippi", "--button:saratoga=Nippissing").run(new InputOutput()), 0);
    }
}
