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
    
    public ResolutionPart(Include include) {
        this(include.getArtifact(), include.getExcludes());
    }
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
        for (Include include : Artifacts.read(new File(entry.getDirectory(), entry.getArtifact().getPath("dep")))) {
            if (!excludes.contains(include.getArtifact())) {
                Set<Artifact> subExcludes = new HashSet<Artifact>();
                subExcludes.addAll(excludes);
                subExcludes.addAll(include.getExcludes());
                additional.add(new ResolutionPart(include.getArtifact(), subExcludes));
            }
        }
        return Collections.<PathPart>singletonList(new ArtifactPart(entry.getDirectory(), entry.getArtifact()));
    }
    
    public Object getKey() {
        return artifact.getKey().subList(0, 2);
    }
}
