package com.goodworkalan.go.go;

import java.util.ArrayList;
import java.util.List;


public class Transaction implements Bundle {
    public final List<Repository> repositories = new ArrayList<Repository>();
    
    public final List<Artifact> artifacts = new ArrayList<Artifact>();
    
    public Transaction() {
    }
    
    public Transaction(List<Repository> repositories, List<Artifact> artifacts) {
        this.repositories.addAll(repositories);
        this.artifacts.addAll(artifacts);
    }
    
    public List<Repository> getRepositories() {
        return repositories;
    }

    public void repsitory(Repository repository) {
        repositories.add(repository);
    }
    
    public void include(Artifact artifact) {
        artifacts.add(artifact);
    }

    public List<Artifact> getArtifacts() {
        return artifacts;
    }
}
