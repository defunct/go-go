package com.goodworkalan.go.go.library;

import static com.goodworkalan.go.go.GoException.INVALID_EXCLUDE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.goodworkalan.go.go.GoException;

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
    private final Set<List<String>> excludes;
    
    /** Whether or not the include is optional. */
    private final boolean optional;

    /**
     * Create a new exclude by splitting the group and project out of the slash
     * delimited exclude token string.
     * 
     * @param exclude The exclude token string.
     */
    public static List<String> exclude(String exclude) {
        String[] split = exclude.split("/");
        if (split.length != 2) {
            throw new GoException(INVALID_EXCLUDE, exclude);
        }
        return Arrays.asList(split[0], split[1]);
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
    public Include(boolean optional, Artifact artifact, List<String>...excludes) {
        this(optional, artifact, Arrays.asList(excludes));
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
    public Include(boolean optional, Artifact artifact, Collection<List<String>> excludes) {
        this.optional = optional;
        this.artifact = artifact;
        this.excludes = new HashSet<List<String>>(excludes);
    }
    
    /**
     * Create an include structure that is not optional.
     * 
     * @param artifact
     *            The artifact to include.
     * @param excludes
     *            The artifact dependencies to exclude.
     */
    public Include(Artifact artifact, List<String>...excludes) {
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
    public Include(Artifact artifact, Collection<List<String>> excludes) {
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
    public Set<List<String>> getExcludes() {
        return excludes;
    }

    /**
     * Return a list of the lines that represent this include in an artifacts
     * file.
     * 
     * @return A list of artifact file lines.
     */
    public List<String> getArtifactFileLines() {
        List<String> lines = new ArrayList<String>();
        lines.add((optional ? "~" : "+") + " " + artifact);
        for (List<String> exclude : excludes) {
            lines.add("- " + exclude.get(0) + "/" + exclude.get(1));
        }
        return lines;
    }
}
