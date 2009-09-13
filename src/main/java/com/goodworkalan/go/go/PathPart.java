package com.goodworkalan.go.go;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

public interface PathPart {
    public File getFile();
    
    public URL getURL() throws MalformedURLException;
    
    public Artifact getArtifact();
    
    public PathPart expand(Library library, Collection<PathPart> additional);
    
    public Object getKey();
}