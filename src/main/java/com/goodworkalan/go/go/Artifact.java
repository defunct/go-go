package com.goodworkalan.go.go;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


/**
 * A file needed for the build, usually a jar file.
 *
 * @author Alan Gutierrez
 */
public class Artifact {
    /** The artifact group. */
    private final String group;

    /** The artifact name. */
    private final String name;

    /** The artifact version. */
    private final String version;

    /**
     * Create a dependency.
     * 
     * @param group
     *            The dependency group.
     * @param name
     *            The dependency name.
     * @param version
     *            The dependency version.
     */
    public Artifact(String group, String name, String version) {
        this.group = group;
        this.name = name;
        this.version = version;
    }

    /**
     * Create an artifact by parsing the given file name assuming the given
     * suffix and extension.
     * 
     * @param file
     *            An artifact file.
     * @param suffix
     *            A file name suffix.
     * @param extension
     *            The file extension.
     */
    public static Artifact parse(File file) {
        LinkedList<File> parts = new LinkedList<File>();
        File directory = file.getParentFile();
        while (directory != null) {
            parts.addFirst(directory);
            directory = directory.getParentFile();
        }
        if (parts.size() < 3) {
            return null;
        }
        String version = parts.removeLast().getName();
        String name = parts.removeLast().getName();
        StringBuilder group = new StringBuilder();
        String separator = "";
        for (File part : parts) {
            group.append(separator).append(part.getName());
            separator = ".";
        }
        return new Artifact(group.toString(), name, version);
    }
    
    public static Artifact parse(String artifact) {
        List<String> parts = new ArrayList<String>(Arrays.asList(artifact.split("/")));
        if (parts.size() == 2) {
            parts.add("*");
        }
        if (parts.size() == 3) {
            return new Artifact(parts.get(0), parts.get(1), parts.get(2));
        }
        return null;
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
     * Get the artifact group.
     * 
     * @return The artifact group.
     */
    public String getGroup() {
        return group;
    }
    
    /**
     * Get the artifact name.
     * 
     * @return The artifact name.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the artifact version.
     * 
     * @return The artifact version.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Get the artifact file name. The artifact file name is the concatenation
     * of the artifact name and the artifact version separated by a hyphen. The
     * suffix provided is catenated after the version spearated by a hyphen.
     * 
     * @param suffix
     *            A suffix to apply or an empty string.
     * @param extension
     *            The file extension to use.
     * @return The file name for this artifact.
     */
    public String getFileName(String suffix, String extension) {
        StringBuilder file = new StringBuilder();
        file.append(name).append("-").append(version)
            .append(suffix.length() == 0 ? "" : "-").append(suffix)
            .append(".").append(extension);
        return file.toString();
    }
    /**
     * Create the path in the repository where the artifact can be found.
     * 
     * @return The file path of the artifacts.
     */
    public String getPath(String suffix, String extension) {
        StringBuilder file = new StringBuilder();
        file.append(group.replace(".", "/"))
                .append("/").append(name)
                .append("/").append(version)
                .append("/")
                    .append(name).append("-").append(version)
                    .append(suffix.length() == 0 ? "" : "-").append(suffix)
                    .append(".").append(extension);
        return file.toString();
    }

    /**
     * This artifact is equal to the given object if it is also an artifact and
     * if the group, name and versions are equal.
     * 
     * @param object
     *            The object to test for equality.
     * @return True if this object equals the given object.
     */
    public boolean equals(Object object) {
        if (object instanceof Artifact) {
            Artifact artifact = (Artifact) object;
            return group.equals(artifact.group)
                && name.equals(artifact.name)
                && version.equals(artifact.version);
        }
        return false;
    }

    /**
     * The hash code combines the hash codes of the group, name and version.
     * 
     * @return The hash code.
     */
    public int hashCode() {
        int hashCode = 1;
        hashCode = hashCode * 37 + group.hashCode();
        hashCode = hashCode * 37 + name.hashCode();
        hashCode = hashCode * 37 + version.hashCode();
        return hashCode;
    }
    
    /**
     * Return the artifact key for this artifact.
     * 
     * @return A string representation of this artifact.
     */
    @Override
    public String toString() {
        return getKey() + "/" + version;
    }
}
