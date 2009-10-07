package com.goodworkalan.go.go;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class Transaction implements Bundle {    
    private final Map<List<String>, Artifact> includes = new LinkedHashMap<List<String>, Artifact>();
    
    private final Set<Artifact> excludes = new HashSet<Artifact>();;
    
    public Transaction() {
    }
    
    public Collection<Artifact> getArtifacts() {
        return includes.values();
    }
    
    public void include(Artifact artifact) {
        List<String> key = artifact.getKey().subList(0, 2);
        if (!includes.containsKey(key)) {
            includes.put(key, artifact);
        }
    }
    
    public Set<Artifact> getExcludes() {
        return excludes;
    }
    
    public void exclude(Artifact exclude) {
        excludes.add(exclude);
    }

    public Collection<PathPart> getPathParts() {
        Collection<PathPart> pathParts = new ArrayList<PathPart>();
        for (Artifact include : includes.values()) {
            pathParts.add(new ResolutionPart(include, excludes));
        }
        return pathParts;
    }
}
