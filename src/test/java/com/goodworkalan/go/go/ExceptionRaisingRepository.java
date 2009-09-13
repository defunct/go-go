package com.goodworkalan.go.go;

import java.net.URI;


public class ExceptionRaisingRepository implements RepositoryClient {
    public ExceptionRaisingRepository(URI uri) {
        throw new UnsupportedOperationException();
    }
    
    public void fetch(Library library, Artifact artifact, String suffix, String extension) {
    }
    
    public void fetchDependencies(Library library, Artifact artifact) {
    }
}
