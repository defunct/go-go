package com.goodworkalan.go.go;

import java.net.URI;

public class FlatRepository implements Repository {
    private final URI uri;
    
    public FlatRepository(URI uri) {
        this.uri = uri;
    }
    
    public void fetch(Artifact artifact, Library library) {
    }
}
