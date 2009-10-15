package com.goodworkalan.go.go;

import java.io.File;
import java.net.MalformedURLException;
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
    
    public File getFile() {
        return new File(libraryDirectory, artifact.getPath("jar"));
    }
    
    public URL getURL() throws MalformedURLException {
        return new URL("jar:" + getFile().toURL().toExternalForm() + "!/");
    }
    
    public Artifact getArtifact() {
        return artifact;
    }

    public Collection<PathPart> expand(Library library, Collection<PathPart> expand) {
        return Collections.<PathPart>singletonList(this);
    }
    
    public Object getKey() {
        return artifact.getKey().subList(0, 2);
    }
}
