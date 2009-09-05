package com.goodworkalan.go.go;

public interface Repository {
    public void fetch(Library library, Artifact artifact, String suffix, String extension);
    
    public void fetchDependencies(Library library, Artifact artifact);
}
