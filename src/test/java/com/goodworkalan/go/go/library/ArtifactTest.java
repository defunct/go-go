package com.goodworkalan.go.go.library;

import static com.goodworkalan.go.go.GoException.MALFORMED_ARTIFACT;
import static com.goodworkalan.go.go.GoException.MALFORMED_ARTIFACT_FILE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import java.io.File;
import java.util.Collections;

import org.testng.annotations.Test;

import com.goodworkalan.go.go.GoException;

/**
 * Unit tests for the Artifact class.
 *
 * @author Alan Gutierrez
 */
public class ArtifactTest {
    /** Test file name generation. */
    @Test
    public void fileName() {
        Artifact artifact = new Artifact("com.goodworkalan", "go-go", "1.0.0");
        assertEquals(artifact.getFileName("jar"), "go-go-1.0.0.jar");
        assertEquals(artifact.getFileName("javadoc/jar"), "go-go-1.0.0-javadoc.jar");
    }
    
    /** Test file path generation. */
    @Test
    public void filePath() {
        Artifact artifact = new Artifact("com.goodworkalan", "go-go", "1.0.0");
        assertEquals(artifact.getPath("jar"), "com/goodworkalan/go-go/1.0.0/go-go-1.0.0.jar");
        assertEquals(artifact.getPath("javadoc/jar"), "com/goodworkalan/go-go/1.0.0/go-go-1.0.0-javadoc.jar");
    }
    
    /** Test string representation. */
    @Test
    public void asString() {
        Artifact artifact = new Artifact("com.goodworkalan", "go-go", "1.0.0");
        assertEquals(artifact.toString(), "com.goodworkalan/go-go/1.0.0");
    }
    
    /** Test parsing an unversioned artifact. */
    @Test
    public void parseUnversioned() {
        Artifact artifact = new Artifact("com.github.bigeasy.go-go/go-go");
        assertEquals(artifact.getVersion(), "+0");
    }
    
    /** Test the file based constructor. */
    @Test
    public void fileConstructor() {
        Artifact artifact = new Artifact(new File("com/github/bigeasy/go-go/go-go/1.0.2/go-go-1.0.2.jar"));
        assertEquals(artifact.getGroup(), "com.github.bigeasy.go-go");
        assertEquals(artifact.getName(), "go-go");
        assertEquals(artifact.getVersion(), "1.0.2");
    }
    
    /** Test an invalid repository file. */
    @Test(expectedExceptions = GoException.class)
    public void failedFIeldConsstructor() {
        exceptional(new Runnable() {
            public void run() {
                new Artifact(new File("a"));
            }
        }, MALFORMED_ARTIFACT_FILE, "Unable to derive an artifact name from the repository file [a].");
    }
    
    /** Test an invalid artifact string. */
    @Test(expectedExceptions = GoException.class)
    public void failedConsstructor() {
        exceptional(new Runnable() {
            public void run() {
                new Artifact("a");
            }
        }, MALFORMED_ARTIFACT, "Unable to parse artifact string [a].");
    }
    
    /** Test directory path generation. */
    @Test
    public void directoryPath() {
        Artifact artifact = new Artifact("com.github.bigeasy.go-go/go-go/1.0.2");
        assertEquals(artifact.getDirectoryPath(), "com/github/bigeasy/go-go/go-go/1.0.2");
    }
    
    /** Test a bad file suffix. */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void badSuffix() {
        Artifact artifact = new Artifact("com.github.bigeasy.go-go/go-go/1.0.2");
        artifact.getPath("a/b/c");
    }
    
    /** Test equality. */
    @Test
    public void equality() {
        Artifact artifact = new Artifact("com.github.bigeasy.go-go/go-go/1.0.2");
        Artifact group = new Artifact("com.goodworkalan-go/go-go/1.0.2");
        Artifact name = new Artifact("com.github.bigeasy.go-go/go-go-boot/1.0.2");
        Artifact version = new Artifact("com.github.bigeasy.go-go/go-go/1.0.3");
        assertFalse(artifact.equals(group));
        assertEquals(artifact, artifact);
        assertEquals(artifact, new Artifact("com.github.bigeasy.go-go/go-go/1.0.2"));
        assertFalse(artifact.equals(name));
        assertFalse(artifact.equals(version));
        assertFalse(artifact.equals("a"));
        assertEquals(artifact.hashCode(), new Artifact("com.github.bigeasy.go-go/go-go/1.0.2").hashCode());
    }
    
    /** Test artifact line. */
    @Test
    public void artifactLines() {
        Artifact artifact = new Artifact("com.github.bigeasy.go-go/go-go/1.0.2");
        assertEquals(artifact.getArtifactsFileLine(Collections.<Exclude>emptySet()), "~ com.github.bigeasy.go-go/go-go/1.0.2");
    }

    /**
     * Run the given runnable and catch a go exception, asserting that the
     * exception message key and message are equal to the given message key and
     * given message.
     * 
     * @param runnable
     *            The exception throwing code.
     * @param messageKey
     *            The expected message key.
     * @param message
     *            The expected message.
     */
    static void exceptional(Runnable runnable, int code, String message) { 
        try {
            runnable.run();
        } catch (GoException e) {
            assertEquals(e.getCode(), code);
            assertEquals(e.getMessage(), message);
            throw e;
        }
    }
}
