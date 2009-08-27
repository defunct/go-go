package com.goodworkalan.go.go;

import static org.testng.Assert.*;
import org.testng.annotations.Test;

public class ArtifactTest {
    @Test
    public void fileName() {
        Artifact artifact = new Artifact("com.goodworkalan", "go-go", "1.0.0");
        assertEquals(artifact.getFileName("", "jar"), "go-go-1.0.0.jar");
        assertEquals(artifact.getFileName("javadoc", "jar"), "go-go-1.0.0-javadoc.jar");
    }
    
    @Test
    public void filePath() {
        Artifact artifact = new Artifact("com.goodworkalan", "go-go", "1.0.0");
        assertEquals(artifact.getPath("", "jar"), "com/goodworkalan/go-go/1.0.0/go-go-1.0.0.jar");
        assertEquals(artifact.getPath("javadoc", "jar"), "com/goodworkalan/go-go/1.0.0/go-go-1.0.0-javadoc.jar");
    }
    
    
    @Test
    public void asString() {
        Artifact artifact = new Artifact("com.goodworkalan", "go-go", "1.0.0");
        assertEquals(artifact.toString(), "com.goodworkalan/go-go/1.0.0");
    }
}
