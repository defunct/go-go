package com.goodworkalan.go.go;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ResolutionPart implements PathPart {
    private final Artifact artifact;
    
    private final Set<Artifact> excludes;
    
    private final List<Repository> repositories;
    
    public ResolutionPart(Artifact artifact, Set<Artifact> excludes, List<Repository> repositories) {
        this.artifact = artifact;
        this.excludes = excludes;
        this.repositories = repositories;
    }
    
    public ResolutionPart(Artifact artifact) {
        this(artifact, Collections.<Artifact>emptySet(), Collections.<Repository>emptyList());
    }
    
    public File getFile() {
        throw new UnsupportedOperationException();
    }
    
    public URL getURL() throws MalformedURLException {
        throw new UnsupportedOperationException();
    }
    
    public Artifact getArtifact() {
        return artifact;
    }
    
    public Collection<PathPart> expand(Library library, Collection<PathPart> additional) {
        LibraryEntry entry = library.getEntry(artifact, repositories);
        if (entry == null) {
            throw new GoException(0);
        }
        for (Transaction transaction : Artifacts.read(new File(entry.directory, entry.artifact.getPath("", "dep")))) {
            for (Artifact include : transaction.includes) {
                if (!excludes.contains(include)) {
                    Set<Artifact> subExcludes = new HashSet<Artifact>();
                    subExcludes.addAll(excludes);
                    subExcludes.addAll(transaction.excludes);
                    additional.add(new ResolutionPart(include, subExcludes, transaction.repositories));
                }
            }
        }
        return Collections.<PathPart>singletonList(new ArtifactPart(entry.directory, entry.artifact));
    }
    
    public Object getKey() {
        return artifact.getKey();
    }
}
