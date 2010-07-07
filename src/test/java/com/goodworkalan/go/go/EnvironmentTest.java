package com.goodworkalan.go.go;

import static org.testng.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.goodworkalan.go.go.library.Artifact;
import com.goodworkalan.go.go.library.Library;

/**
 * Unit tests for the {@link Environment} class.
 *
 * @author Alan Gutierrez
 */
public class EnvironmentTest {
    /**
     * Get an <code>Executor</code> for testing.
     * 
     * @return An executor.
     */
    public Executor getExecutor() {
        Map<List<String>, Artifact> programs = new HashMap<List<String>, Artifact>();
        Library library = new Library(new File[0]);
        return new Executor(library, programs, 0);

    }

    /** Test debug output. */
    @Test
    public void debug() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        getExecutor().run(new InputOutput(System.in, System.out, new PrintStream(out)), "snap", "--verbose", "--verbose", "prattle", Collections.singletonMap("token", "greet"));
        assertEquals(out.toString(), "Hello, World!\n");
    }

    /** Test debug output. */
    @Test
    public void noDebug() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        getExecutor().run(new InputOutput(System.in, System.out, new PrintStream(out)), "snap", "--verbose", "prattle", new Object[] { "token", "foo" });
        assertEquals(out.toString(), "");
    }
    
    /** Test missing message key. */
    @Test
    public void missingKey() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        getExecutor().run(new InputOutput(System.in, System.out, new PrintStream(out)), "snap", "--verbose", "--verbose", "prattle", new Object[] { "--token=foo" });
        assertEquals(out.toString(), "Error message [PrattleCommand/foo] missing. This is a meta-error message.\n");
    }
    
    /** Test missing message arguments. */
    @Test
    public void missingArguments() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        getExecutor().run(new InputOutput(System.in, System.out, new PrintStream(out)), "snap", "--verbose", "--verbose", "prattle", "--token=missingArguments");
        assertEquals(out.toString(), "Error message format argument missing. Format specifier 'd'. This is a meta-error message.\n");
    }


    /** Test a missing bundle. */
    @Test
    public void missingBundle() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        getExecutor().run(new InputOutput(System.in, System.out, new PrintStream(out)), "snap", "--verbose", "no-bundle");
        assertEquals(out.toString(), "Error message bundle missing. This is a meta-error message.\n");
    }
    
    /** Test the get methods. */
    @Test
    public void get() {
        assertEquals(getExecutor().run(new InputOutput(), "snap", "--verbose", "get"), 0);
    }
}
