package com.goodworkalan.go.go.commands;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.Collections;

import org.testng.annotations.Test;

import com.goodworkalan.comfort.io.Files;
import com.goodworkalan.go.go.Go;
import com.goodworkalan.go.go.GoError;
import com.goodworkalan.go.go.InputOutput;
import com.goodworkalan.go.go.Redirection;

/**
 * Unit tests for the {@link InstallCommand} class.
 *
 * @author Alan Gutierrez
 */
public class InstallCommandTest {
    /** Test the welcome message. */
    @Test
    public void execute() {
        Files.delete(new File("src/test/libraries/a/go-go"));
        Go.execute(Collections.singletonList(new File("src/test/libraries/a").getAbsoluteFile()), "boot", "install");
        Redirection redirection = new Redirection();
        Go.execute(Collections.singletonList(new File("src/test/libraries/a").getAbsoluteFile()), redirection.io, "boot", "install", "com.goodworkalan/leaves", "com.goodworkalan/missing");
        assertEquals(redirection.out.toString(), "com.goodworkalan/leaves/0.1 branch, branch leaf\n");
        redirection = new Redirection();
        Go.execute(Collections.singletonList(new File("src/test/libraries/a").getAbsoluteFile()), redirection.io, "boot", "install", "com.goodworkalan/leaves", "com.goodworkalan/missing");
        assertEquals(redirection.out.toString(), "com.goodworkalan/leaves/0.1 branch, branch leaf\n");
        redirection = new Redirection();
        Go.execute(Collections.singletonList(new File("src/test/libraries/a").getAbsoluteFile()), redirection.io, "--verbose", "--verbose", "boot", "com.goodworkalan/leaves", "com.goodworkalan/missing");
    }

    /** Test installing an artifact with no commandables. */
    @Test
    public void noCommandableResource() {
        Go.execute(Collections.singletonList(new File("src/test/libraries/a").getAbsoluteFile()), "boot", "install", "com.goodworkalan/example");
    }

    /** Test a bad zip file. */
    @Test
    public void badZipFile() {
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        InputOutput io = new InputOutput(System.in, System.out, new PrintStream(err));
        Go.execute(Collections.singletonList(new File("src/test/libraries/b").getAbsoluteFile()), io, "boot", "install", "com.goodworkalan/dummy/0.2");
        assertEquals(err.toString(), "Unable to read the JAR archive [/Users/alan/git/go-go/src/test/libraries/b/com/goodworkalan/dummy/0.2/dummy-0.2.jar].\n");
    }
    
    /** Test cannot create resolution directory. */
    @Test
    public void cannotCreateDirectory() {
        File goGo = new File("src/test/libraries/a/go-go");
        try {
            Files.delete(goGo);
            Files.pour(goGo, asList(""));
            Redirection redirection = new Redirection();
            Go.execute(Collections.singletonList(new File("src/test/libraries/a").getAbsoluteFile()), redirection.io, "boot", "install", "com.goodworkalan/leaves", "com.goodworkalan/missing");
            assertEquals(redirection.err.toString(), "Cannot create the artifact boot configuration directory [/Users/alan/git/go-go/src/test/libraries/a/go-go/com.goodworkalan].\n");
        } finally {
            Files.delete(goGo);
        }
    }
    
    /** Test cannot create resolution file. */
    @Test(expectedExceptions = GoError.class)
    public void cannotCreateFile() {
        File goGo = new File("src/test/libraries/a/go-go/com.goodworkalan/leaves.go");
        try {
            Files.delete(goGo);
            goGo.mkdirs();
            WriteInstallCommand.writeCommands("", goGo);
        } catch (GoError e) {
            assertEquals(e.getCode(), GoError.CANNOT_WRITE_BOOT_CONFIGURATION);
            assertEquals(e.getMessage(), "Cannot write the artifact boot configuration file [src/test/libraries/a/go-go/com.goodworkalan/leaves.go].");
            throw e;
        } finally {
            Files.delete(goGo);
        }
    }
    
    /** Test unable to load class. */
    @Test(expectedExceptions = RuntimeException.class)
    public void cannotLoadClass() {
        WriteInstallCommand.loadCommandable(Thread.currentThread().getContextClassLoader(), "com.goodworkalan.missing.DoesNotExist");
    }
    
    /** Test load class that is not commandable. */
    @Test(expectedExceptions = ClassCastException.class)
    public void cannotLoadNotCommandable() {
        WriteInstallCommand.loadCommandable(Thread.currentThread().getContextClassLoader(), "java.lang.String");
    }
}
