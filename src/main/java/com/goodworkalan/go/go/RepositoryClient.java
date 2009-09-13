package com.goodworkalan.go.go;

import java.net.URI;

public interface RepositoryClient {
    public String getTypeName();

    public void fetch(URI uri, Library library, Artifact artifact, String suffix, String extension);
    
    public void fetchDependencies(URI uri, Library library, Artifact artifact);
}
