package com.goodworkalan.go.go;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * An artifact include structure that groups the artifact, its excluded
 * dependencies and whether or not it is optional. 
 *  
 * @author Alan Gutierrez
 */
public class Include {
    /** The artifact to include. */
    private final Artifact artifact;
    
    /** The artifact dependencies to exclude. */
    private final Set<Artifact> excludes;
    
    /** Whether or not the include is optional. */
    private final boolean optional;

    /**
     * Create an include structure.
     * 
     * @param optional
     *            Whether or not the include is optional.
     * @param artifact
     *            The artifact to include.
     * @param excludes
     *            The artifact dependencies to exclude.
     */
    public Include(boolean optional, Artifact artifact, Artifact...excludes) {
        this.optional = optional;
        this.artifact = artifact;
        this.excludes = new HashSet<Artifact>(Arrays.asList(excludes));
    }

    /**
     * Create an include structure.
     * 
     * @param optional
     *            Whether or not the include is optional.
     * @param artifact
     *            The artifact to include.
     * @param excludes
     *            The artifact dependencies to exclude.
     */
    public Include(boolean optional, Artifact artifact, Collection<Artifact> excludes) {
        this(optional, artifact, excludes.toArray(new Artifact[excludes.size()]));
    }
    
    /**
     * Create an include structure that is not optional.
     * 
     * @param artifact
     *            The artifact to include.
     * @param excludes
     *            The artifact dependencies to exclude.
     */
    public Include(Artifact artifact, Artifact...excludes) {
        this(false, artifact, excludes);
    }

    /**
     * Create an include structure that is not optional.
     * 
     * @param artifact
     *            The artifact to include.
     * @param excludes
     *            The artifact dependencies to exclude.
     */
    public Include(Artifact artifact, Collection<Artifact> excludes) {
        this(false, artifact, excludes);
    }

    /**
     * Get whether or not the include is optional.
     * 
     * @return True if the include is optional.
     */
    public boolean isOptional() {
        return optional;
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
     * Get the artifact dependencies to exclude.
     * 
     * @return The artifact dependencies to exclude.
     */
    public Set<Artifact> getExcludes() {
        return excludes;
    }
}
