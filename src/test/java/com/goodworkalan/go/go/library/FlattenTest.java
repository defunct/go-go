package com.goodworkalan.go.go.library;

import static org.testng.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import org.testng.annotations.Test;

import com.goodworkalan.glob.Find;
import com.goodworkalan.go.go.Artifact;
import com.goodworkalan.go.go.CommandInterpreter;
import com.goodworkalan.go.go.Environment;
import com.goodworkalan.go.go.POMReader;

/**
 * Unit test for the lib flatten command.
 * 
 * @author Alan Gutierrez
 */
public class FlattenTest {
    /**
     * Test the flatten command against an example repository.
     */
    @Test
    public void flatten() {
        File repository = new File("src/test/resources/unflattened");
        for (File file : new Find().include("**/*.dep").exclude("**/h2*").find(repository)) {
            new File(repository, file.toString()).delete();
        }
        new CommandInterpreter("src/test/resources/go.go").main("lib", "flatten", "src/test/resources/unflattened/nothing", "src/test/resources/unflattened");
    }

    /**
     * Test the I/O exception handling of the flatten command.
     */
    @Test
    public void ioException() {
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        Environment environment = new Environment(System.in, new PrintStream(err), System.out, new String[0][], new String[0]);
        Artifact artifact = new Artifact("com.lowagie", "itext", "1.1.115");
        new Flatten().flatten(environment, new POMReader(new File("src/test/resources/unflattened")), artifact, new File("src/test/resources/unflatted/nothing/out.deps"));
        assertTrue(err.toByteArray().length != 0);
    }
}
