package com.goodworkalan.go.go;

import java.io.File;

import org.testng.annotations.Test;

/**
 * Test suite for artifacts file.
 *
 * @author Alan Gutierrez
 */
public class ArtifactsFileTest {
    /**
     * Check for an unexpected line start.
     */
    @Test
    public void unknownLineStart() {
        new ArtifactsFileReader(new File("foo"));
    }
}
