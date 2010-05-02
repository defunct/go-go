package com.goodworkalan.go.go;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.io.File;

import org.testng.annotations.Test;

import com.goodworkalan.go.go.library.Artifact;

/**
 * Unit tests for the Artifact class.
 *
 * @author Alan Gutierrez
 */
public class ArtifactTest {
    /** Test extraction of artifact properties from a file path. */
    @Test
    public void parse() {
        File file = new File("com/goodworkalan/go-go/1.0/go-go-1.0.jar");
        Artifact artifact = Artifact.parse(file);
        assertEquals(artifact.getGroup(), "com.goodworkalan");
        assertEquals(artifact.getName(), "go-go");
        assertEquals(artifact.getVersion(), "1.0");
        assertNull(Artifact.parse(new File("com/goodworkalan")));
    }
    
    @Test
    public void classifier() {
        Artifact artifact = new Artifact("org.testng/testng/5.10/jdk15");
        assertEquals(artifact.toString(), "org.testng/testng/5.10/jdk15");
    }

    /** Test file name generation. */
    @Test
    public void fileName() {
        Artifact artifact = new Artifact("com.goodworkalan", "go-go", "1.0.0", "");
        assertEquals(artifact.getFileName("jar"), "go-go-1.0.0.jar");
        assertEquals(artifact.getFileName("javadoc/jar"), "go-go-1.0.0-javadoc.jar");
    }
    
    /** Test file path generation. */
    @Test
    public void filePath() {
        Artifact artifact = new Artifact("com.goodworkalan", "go-go", "1.0.0", "");
        assertEquals(artifact.getPath("jar"), "com/goodworkalan/go-go/1.0.0/go-go-1.0.0.jar");
        assertEquals(artifact.getPath("javadoc/jar"), "com/goodworkalan/go-go/1.0.0/go-go-1.0.0-javadoc.jar");
    }
    
    /** Test string represenation. */
    @Test
    public void asString() {
        Artifact artifact = new Artifact("com.goodworkalan", "go-go", "1.0.0", "");
        assertEquals(artifact.toString(), "com.goodworkalan/go-go/1.0.0");
    }
}
