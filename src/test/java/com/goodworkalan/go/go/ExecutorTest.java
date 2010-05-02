package com.goodworkalan.go.go;

import static org.testng.Assert.assertEquals;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.goodworkalan.go.go.library.Artifact;
import com.goodworkalan.go.go.library.Library;
import com.goodworkalan.reflective.ReflectiveFactory;

/**
 * Unit tests for the {@link Executor} class.
 *
 * @author Alan Gutierrez
 */
public class ExecutorTest {
    private File getLibrary(String library) {
        return new File(new File(".").getAbsolutePath() + File.separator + "src" + File.separator + "test" + File.separator + "libraries" + File.separator + library);
    }

    /** Test the invocation of load. */
    @Test
    public void load() {
        Map<List<String>, Artifact> programs = new HashMap<List<String>, Artifact>();
        programs.put(Arrays.asList("snap"), new Artifact("com.goodworkalan/example/0.1"));
        programs.put(Arrays.asList("snap", "watusi"), new Artifact("com.goodworkalan/dummy/0.1"));
        Library library = new Library(new File[] { getLibrary("a"), getLibrary("b") });
        Executor executor = new Executor(new ReflectiveFactory(), library, programs);
        
        String hello = executor.run(String.class, new InputOutput(), "snap", "watusi", "--repeat=Hello");
        assertEquals(hello, "Hello");
    }
    
    /** Test the invocation of load after command. */
    @Test
    public void loadAfterCommandable() {
        Map<List<String>, Artifact> programs = new HashMap<List<String>, Artifact>();
        Library library = new Library(getLibrary("a"), getLibrary("b"));
        Executor executor = new Executor(new ReflectiveFactory(), library, programs);
        String hello = executor.run(String.class, new InputOutput(), "snap", "--hidden", "watusi", "--repeat=Hello");
        assertEquals(hello, "Hello");
    }
}
