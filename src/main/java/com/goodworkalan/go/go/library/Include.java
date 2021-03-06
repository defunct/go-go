package com.goodworkalan.go.go.library;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import com.goodworkalan.go.go.version.VersionSelector;

/**
 * An artifact include structure that groups the artifact, its excluded
 * dependencies and whether or not it is optional. 
 *  
 * @author Alan Gutierrez
 */
public class Include {
    /** The artifact name. */
    private final Artifact artifact;
    
    /** The version matcher. */
    private final VersionSelector versionSelector;
    
    /** The artifact dependencies to exclude. */
    private final Set<Exclude> excludes;

    /**
     * Create an include structure.
     * 
     * @param artifact
     *            The artifact to include.
     * @param excludes
     *            The artifact dependencies to exclude.
     */
    public Include(Artifact artifact, Collection<Exclude> excludes) {
        this.artifact = artifact;
        this.versionSelector = new VersionSelector(artifact.getVersion());
        this.excludes = new LinkedHashSet<Exclude>(excludes);
    }
    
    /**
     * Create an include structure.
     * 
     * @param artifact
     *            The artifact to include.
     */
    public Include(Artifact artifact) {
        this.artifact = artifact;
        this.versionSelector = new VersionSelector(artifact.getVersion());
        this.excludes = Collections.emptySet();
    }

    /**
     * Create an include structure from the given artifact string excluding
     * artifacts that match the given exclude strings.
     * 
     * @param artifact
     *            The artifact to include.
     * @param excludes
     *            The artifact dependencies to exclude.
     */
    public Include(String artifact, String...excludes) {
        this(new Artifact(artifact), Exclude.excludes(excludes));
    }
    
    /**
     * Get the artifact dependencies to exclude.
     * 
     * @return The artifact dependencies to exclude.
     */
    public Artifact getArtifact() {
        return artifact;
    }
    
    /**
     * Get the version selector for this include.
     * 
     * @return The version selector;
     */
    public VersionSelector getVersionSelector() {
        return versionSelector;
    }

    /**
     * Get the artifact dependencies to exclude.
     * 
     * @return The artifact dependencies to exclude.
     */
    public Set<Exclude> getExcludes() {
        return excludes;
    }

    /**
     * Return a list of the lines that represent this include in an artifacts
     * file.
     * 
     * @return The artifact file line.
     */
    public String getArtifactFileLine() {
        StringBuilder line = new StringBuilder();
        line.append("~ ").append(artifact);
        for (Exclude exclude : excludes) {
            line.append(" ").append(exclude);
        }
        return line.toString();
    }
}
