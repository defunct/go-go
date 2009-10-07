package com.goodworkalan.go.go;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ResolutionPart implements PathPart {
    private final Artifact artifact;
    
    private final Set<Artifact> excludes;
    
    public ResolutionPart(Artifact artifact) {
        this(artifact, Collections.<Artifact>emptySet());
    }

    public ResolutionPart(Artifact include, Set<Artifact> excludes) {
        this.artifact = include;
        this.excludes = excludes;
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
        LibraryEntry entry = library.getEntry(artifact);
        if (entry == null) {
            throw new GoException(0);
        }
        Transaction transaction = Artifacts.read(new File(entry.directory, entry.artifact.getPath("", "dep")));
        for (Artifact artifact : transaction.getArtifacts()) {
            if (!excludes.contains(artifact)) {
                Set<Artifact> subExcludes = new HashSet<Artifact>();
                subExcludes.addAll(excludes);
                subExcludes.addAll(transaction.getExcludes());
                additional.add(new ResolutionPart(artifact, subExcludes));
            }
        }
        return Collections.<PathPart>singletonList(new ArtifactPart(entry.directory, entry.artifact));
    }
    
    public Object getKey() {
        return artifact.getKey().subList(0, 2);
    }
}
