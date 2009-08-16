package com.goodworkalan.go;

import go.go.Path;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Artifact {
    /** The list of dependencies that depend on this item. */
    private final List<Artifact> dependents;

    /** The list of dependencies for this dependency. */
    private final List<Artifact> dependencies;

    /** The dependency group. */
    private final String group;

    /** The dependency name. */
    private final String name;

    /** The dependency version. */
    private final String version;

    /** Where the dependency is used. */
    private final String usage;

    /** Whether we should ever use a snapshot. */
    private final boolean snapshot;

    /**
     * Create a dependency.
     * 
     * @param group
     *            The dependency group.
     * @param name
     *            The dependency name.
     * @param version
     *            The dependency version.
     * @param usage
     *            Where the dependency is used.
     * @param snapshot
     *            Whether we should ever use a snapshot.
     */
    public Artifact(String group, String name, String version, String usage, boolean snapshot) {
        this.group = group;
        this.name = name;
        this.version = version;
        this.usage = usage;
        this.snapshot = snapshot;
        this.dependencies = new LinkedList<Artifact>();
        this.dependents = new LinkedList<Artifact>();
    }

    /**
     * Get where the dependency is used.
     * 
     * @return Where the dependency is used.
     */
    public String getUsage() {
        return usage;
    }

    /**
     * Get a key that uniquely defines the artifact without version.
     * 
     * @return A key.
     */
    public String getKey() {
        return group + "/" + name;
    }

    /**
     * Create the path in the repository where the artifact can be found.
     * 
     * @return The file path of the artifacts.
     */
    public String getPath(String suffix, String extension, boolean wantSnapshots) {
        StringBuilder file = new StringBuilder();
        file.append(group.replace(".", "/"))
                .append("/").append(name)
                .append("/").append(version)
                .append("/")
                    .append(name).append("-").append(version)
                    .append(wantSnapshots && snapshot ? "-SNAPSHOT" : "")
                    .append(suffix.length() == 0 ? "" : "-").append(suffix)
                    .append(".").append(extension);
        return file.toString();
    }

    /**
     * Make this dependency dependent on the given dependency.
     * 
     * @param dependency
     *            The dependency.
     */
    public void depends(Artifact dependency) {
        dependency.dependents.add(this);
        dependencies.add(dependency);
    }

    /**
     * Find the minimum depth of this depdendency.
     * 
     * @return The minimum depth of this dependency.
     */
    public int getMinimumDepth() {
        if (dependents.isEmpty()) {
            return 0;
        }
        int minimum = Integer.MAX_VALUE;
        for (Artifact dependency : dependents) {
            int depth = dependency.getMinimumDepth();
            if (depth < minimum) {
                minimum = depth;
            }
        }
        return minimum;
    }
    
    private void getDependencies(Set<String> usages, List<Artifact> artifacts, List<Artifact> dependencies, Set<String> seen) {
        if (!dependencies.isEmpty()) {
            int start = artifacts.size();
            for (Artifact dependency : dependencies) {
                if (usages.contains(dependency.getUsage()) && !seen.contains(dependency.getKey())) {
                    seen.add(dependency.getKey());
                    artifacts.add(dependency);
                }
            }
            Set<String> subUsages = new HashSet<String>(usages);
            subUsages.add("core");
            for (Artifact dependency : artifacts.subList(start, artifacts.size())) {
                getDependencies(subUsages, artifacts, dependency.dependencies, seen);
            }
        }
    }
    
    public List<Artifact> getDependencies(String...usages) {
        List<Artifact> artifacts = new LinkedList<Artifact>();
        getDependencies(new HashSet<String>(Arrays.asList(usages)), artifacts, Collections.singletonList(this), new HashSet<String>());
        return artifacts;
    }
    
    public Path getClassPath(File dir, boolean wantSnapshots, String...usages) {
        List<File> path = new ArrayList<File>();
        for (Artifact artifact : getDependencies(usages)) {
            path.add(new File(dir, artifact.getPath("", "jar", wantSnapshots)));
        }
        return new Path(path);
    }
}
