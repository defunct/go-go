package com.goodworkalan.go.go;

import static com.goodworkalan.go.go.ExcludeTest.exceptional;
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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import org.testng.annotations.Test;

import com.goodworkalan.go.go.library.Artifact;
import com.goodworkalan.go.go.library.Library;

/**
 * Unit tests for the {@link Executor} class.
 *
 * @author Alan Gutierrez
 */
public class ExecutorTest {
    /**
     * Create a library file by appending the relative path to the test library
     * directory.
     * 
     * @param library
     *            The relative path to the test library.
     * @return The absolute path to the test library.
     */
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
        Executor executor = new Executor(library, programs, 0);
        
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
    @Test(expectedExceptions = GoException.class)
    public void cannotEnumerateCommandables() {
        exceptional(COMMANDABLE_RESOURCES_IO, new Runnable() {
            public void run() {
                try {
                    Executor executor = getExecutor();
                    ClassLoader classLoader = mock(ClassLoader.class);
                    when(classLoader.getResources(anyString())).thenThrow(new IOException());
                    executor.readConfigurations(classLoader, new InputOutput());
                } catch (IOException e) {
                }
            }
        });
    }
    
    /** Test unable to enumerate command line resources. */
    @Test(expectedExceptions = GoException.class)
    public void cannotReadCommandles() {
        exceptional(COMMANDABLE_RESOURCE_IO, new Runnable() {
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
        });
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

    /**
     * Create an executor that will search for commands in two of the test
     * libraries.
     * 
     * @return An executor.
     */
    private Executor getExecutor() {
        Map<List<String>, Artifact> programs = new HashMap<List<String>, Artifact>();
        Library library = new Library(getLibrary("a"), getLibrary("b"));
        Executor executor = new Executor(library, programs, 0);
        return executor;
    }
    
    /**
     * Test retrying a callable that throws an exception that is not an
     * <code>InterruptedException</code>.
     */
    @Test(expectedExceptions = RuntimeException.class)
    public void retryException() {
        Executor.retry(new Callable<Object>() {
            public Object call() throws IOException {
                throw new IOException();
            }
        });
    }
    
    /** Test callable retry. */
    @Test
    public void function() throws InterruptedException {
        Thread thread = new Thread() {
            public void run() {
                String a = Executor.retry(new Callable<String>() {
                    int count;
                    public String call() throws InterruptedException {
                        if (count == 0) {
                            count++;
                            synchronized (this) {
                                wait();
                            }
                        }
                        return "A";
                    }
                });
                assertEquals(a, "A");
            }
        };
        thread.start();
        thread.interrupt();
        thread.join();
    }
    
    /** Test future retry. */
    @Test
    public void future() throws InterruptedException {
        final FutureTask<Integer> future = new FutureTask<Integer>(new Callable<Integer>() {
            public Integer call() {
                return 1;
            }
        });
        Thread consumer = new Thread() {
            @Override
            public void run() {
                int i = 0;
                try {
                    i = Executor.retry(future);
                } catch (ExecutionException e) {
                }
                assertEquals(i, 1);
            }
        };
        consumer.start();
        consumer.interrupt();
        Thread thread = new Thread(future);
        thread.start();
        consumer.join();
        thread.join();
    }
}
