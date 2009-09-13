package com.goodworkalan.go.go;

import java.io.File;

public class LibraryEntry {
    public File directory;
    
    public Artifact artifact;
    
    public LibraryEntry(File directory, Artifact artifact) {
        this.directory = directory;
        this.artifact = artifact;
    }
}
