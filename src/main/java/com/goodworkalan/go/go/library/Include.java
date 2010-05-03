package com.goodworkalan.go.go.library;

import static com.goodworkalan.go.go.GoException.INVALID_EXCLUDE;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.goodworkalan.go.go.GoException;
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
    private final Set<List<String>> excludes;
    
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
     * @param artifact
     *            The artifact to include.
     * @param excludes
     *            The artifact dependencies to exclude.
     */
    public Include(Artifact artifact, Collection<List<String>> excludes) {
        this.artifact = artifact;
        this.versionSelector = new VersionSelector(artifact.getVersion());
        this.excludes = new HashSet<List<String>>(excludes);
    }
    
    /**
     * Create an include structure.
     * 
     * @param artifact
     *            The artifact to include.
     * @param excludes
     *            The artifact dependencies to exclude.
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
        this.artifact = new Artifact(artifact);
        this.versionSelector = new VersionSelector(this.artifact.getVersion());
        this.excludes = new HashSet<List<String>>();
        for (String exclude : excludes) {
            this.excludes.add(exclude(exclude));
        }
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
    public Set<List<String>> getExcludes() {
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
        for (List<String> exclude : excludes) {
            line.append(" ").append(exclude.get(0)).append("/").append(exclude.get(1));
        }
        return line.toString();
    }
}
