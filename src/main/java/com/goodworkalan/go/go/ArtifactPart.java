package com.goodworkalan.go.go;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;

public class ArtifactPart implements PathPart {
    private final File libraryDirectory;
    private final Artifact artifact;
    
    public ArtifactPart(File libraryDirectory, Artifact artifact) {
        this.libraryDirectory = libraryDirectory;
        this.artifact = artifact;
    }

    /**
     * Get the directory of the library where the artifact was found.
     * 
     * @return The library directory.
     */
    public File getLibraryDirectory() {
        return libraryDirectory;
    }
    
    public File getFile() {
        return new File(libraryDirectory, artifact.getPath("jar"));
    }

    /**
     * Get the jar protocol URL for this artifact part.
     * 
     * @return The URL.
     */
    public URL getURL() {
        return PathParts.toURL("jar:" + PathParts.toURL(getFile().toURI().toString()).toExternalForm() + "!/");
    }
    
    public Artifact getArtifact() {
        return artifact;
    }

    public Collection<PathPart> expand(Library library, Collection<PathPart> expand) {
        return Collections.<PathPart>singletonList(this);
    }
    
    public Object getUnversionedKey() {
        return artifact.getKey().subList(0, 2);
    }
}
