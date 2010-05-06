package com.goodworkalan.go.go.library;

import static com.goodworkalan.go.go.GoException.UNRESOLVED_ARTIFACT;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.goodworkalan.go.go.GoException;

public class ResolutionPart extends ExpandingPathPart {
    private final Include include;
    
    public ResolutionPart(Include include) {
        this.include = include;
    }

    public ResolutionPart(Artifact artifact) {
        this(new Include(artifact));
    }

    public ResolutionPart(Artifact artifact, Set<Exclude> excludes) {
        this(new Include(artifact, excludes));
    }
    
    public Artifact getArtifact() {
        return include.getArtifact();
    }
    
    public void expand(Library library, Collection<PathPart> expanded, Collection<PathPart> expand) {
        ArtifactPart artifactPart = library.getArtifactPart(include, "jar", "dep");
        if (artifactPart == null) {
            throw new GoException(UNRESOLVED_ARTIFACT, include.getArtifact());
        }
        expanded.add(artifactPart);
        for (Include subInclude : Artifacts.read(new File(artifactPart.getLibraryDirectory(), artifactPart.getArtifact().getPath("dep")))) {
            if (!include.getExcludes().contains(subInclude.getArtifact().getUnversionedKey())) {
                Set<Exclude> subExcludes = new HashSet<Exclude>();
                subExcludes.addAll(include.getExcludes());
                subExcludes.addAll(subInclude.getExcludes());
                expand.add(new ResolutionPart(subInclude.getArtifact(), subExcludes));
            }
        }
    }
    
    public Object getUnversionedKey() {
        return include.getArtifact().getUnversionedKey();
    }
    
    public Set<Exclude> getExcludes() {
        return include.getExcludes();
    }
    
    public String toString() {
        return include.getArtifact().toString();
    }
}
