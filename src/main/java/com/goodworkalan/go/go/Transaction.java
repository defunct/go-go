package com.goodworkalan.go.go;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


public class Transaction implements Bundle {
    public final List<RepositoryLine> repositories = new ArrayList<RepositoryLine>();
    
    public final Set<Artifact> includes = new LinkedHashSet<Artifact>();
    
    public final Set<Artifact> excludes = new HashSet<Artifact>();
    
    public Transaction() {
    }
    
    public Transaction(List<RepositoryLine> repositories, List<Artifact> artifacts) {
        this.repositories.addAll(repositories);
        this.includes.addAll(artifacts);
    }
    
    public List<RepositoryLine> getRepositories() {
        return repositories;
    }

    public void repsitory(String type, URI uri) {
        repositories.add(new RepositoryLine(type, uri));
    }
    
    public void include(Artifact artifact) {
        includes.add(artifact);
    }
    
    public void exclude(Artifact exclude) {
        excludes.add(exclude);
    }

    public Collection<PathPart> getPathParts() {
        Collection<PathPart> pathParts = new ArrayList<PathPart>();
        for (Artifact include : includes) {
            pathParts.add(new ResolutionPart(include, excludes, repositories));
        }
        return pathParts;
    }
}
