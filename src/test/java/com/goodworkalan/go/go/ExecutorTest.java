package com.goodworkalan.go.go;

import static com.goodworkalan.go.go.GoException.COMMANDABLE_RESOURCES_IO;
import static com.goodworkalan.go.go.GoException.COMMANDABLE_RESOURCE_IO;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ThreadPoolExecutor;

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
        Library library = new Library(getLibrary("a"), getLibrary("b"));
        ProgramThreadFactory threadFactory = new ProgramThreadFactory();
        ThreadPoolExecutor threadPool = ProgramQueue.getThreadPoolExecutor(threadFactory);
        Executor executor = new Executor(new ReflectiveFactory(), library, programs, threadFactory, threadPool, 0);
        
        String hello = executor.run(String.class, new InputOutput(), "snap", "watusi", "--repeat=Hello");
        assertEquals(hello, "Hello");
    }
    
    /** Test the invocation of load after command. */
    @Test
    public void loadAfterCommandable() {
        Executor executor = getExecutor();
        String hello = executor.run(String.class, new InputOutput(), "snap", "--hidden", "watusi", "--repeat=Hello");
        assertEquals(hello, "Hello");
    }
    
    /** Test unable to enumerate command line resources. */
    @Test
    public void cannotEnumerateCommandables() {
        new GoExceptionCatcher(COMMANDABLE_RESOURCES_IO, new Runnable() {
            public void run() {
                try {
                    Executor executor = getExecutor();
                    ClassLoader classLoader = mock(ClassLoader.class);
                    when(classLoader.getResources(anyString())).thenThrow(new IOException());
                    executor.readConfigurations(classLoader, new InputOutput());
                } catch (IOException e) {
                }
            }
        }).run();
    }
    
    /** Test unable to enumerate command line resources. */
    @Test
    public void cannotReadCommandles() {
        new GoExceptionCatcher(COMMANDABLE_RESOURCE_IO, new Runnable() {
            public void run() {
                try {
                    Executor executor = getExecutor();
                    ClassLoader classLoader = mock(ClassLoader.class);
                    URL url = new URL("file:///u:n\\;li<k>e!ly");
                    Vector<URL> vector = new Vector<URL>();
                    vector.add(url);
                    when(classLoader.getResources(anyString())).thenReturn(vector.elements());
                    executor.readConfigurations(classLoader, new InputOutput());
                } catch (IOException e) {
                }
            }
        }).run();
    }
    
    /** Command not found. */
    @Test
    public void commandNotFound() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(new String("com.missing.Commandable\n").getBytes("UTF-8"));
        InputOutput io = new InputOutput(System.in, System.out, new PrintStream(out));
        getExecutor().readCommandables(Thread.currentThread().getContextClassLoader(), in, io);
        assertEquals(out.toString(), "WARNING: Cannot find command class com.missing.Commandable.\n");
    }
    
    /** Command does not implement commandable. */
    @Test
    public void notCommandable() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(new String("java.lang.String\n").getBytes("UTF-8"));
        InputOutput io = new InputOutput(System.in, System.out, new PrintStream(out));
        getExecutor().readCommandables(Thread.currentThread().getContextClassLoader(), in, io);
        assertEquals(out.toString(), "WARNING: Command class java.lang.String does not implement com.goodworkalan.go.go.Commandable.\n");
    }

    private Executor getExecutor() {
        Map<List<String>, Artifact> programs = new HashMap<List<String>, Artifact>();
        Library library = new Library(getLibrary("a"), getLibrary("b"));
        ProgramThreadFactory threadFactory = new ProgramThreadFactory();
        ThreadPoolExecutor threadPool = ProgramQueue.getThreadPoolExecutor(threadFactory);
        Executor executor = new Executor(new ReflectiveFactory(), library, programs, threadFactory, threadPool, 0);
        return executor;
    }
}
