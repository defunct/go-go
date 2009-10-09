package com.goodworkalan.go.go;

import java.io.File;

/**
 * A structure representing a library artifact and the directory of the library
 * where the artifact was found in the library path.
 * 
 * @author Alan Gutierrez
 */
public class LibraryEntry {
    /** The library directory. */
    private File directory;

    /** The artifact. */
    private Artifact artifact;

    /**
     * Create a new library entry.
     * 
     * @param directory
     *            The library directory.
     * @param artifact
     *            The artifact.
     */
    public LibraryEntry(File directory, Artifact artifact) {
        this.directory = directory;
        this.artifact = artifact;
    }

    /**
     * Get the library directory.
     * 
     * @return The library directory.
     */
    public File getDirectory() {
        return directory;
    }

    /**
     * Get the artifact.
     * 
     * @return The artifact.
     */
    public Artifact getArtifact() {
        return artifact;
    }
}
