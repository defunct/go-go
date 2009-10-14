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
    
    public Artifact(String artifact) {
        List<String> parts = new ArrayList<String>(Arrays.asList(artifact.split("/")));
        if (parts.size() == 2) {
            parts.add("*");
        }
        if (parts.size() != 3) {
            throw new GoException(0);
        }
        this.group = parts.get(0);
        this.name = parts.get(1);
        this.version = parts.get(2);
    }
    
    /**
     * Get a key that is a list that uniquely defines the artifact.
     * 
     * @return A key as a list of name, group, version.
     */
    public List<String> getKey() {
        List<String> key =  new ArrayList<String>();
        key.add(name);
        key.add(group);
        key.add(version);
        return key;
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
     * Create the file name for the artifact file with the given suffix and the
     * given file extension. The file name takes the form of following the
     * pattern where the dots in the group are replaced with file part
     * separators.
     * 
     * <code><pre>name-version-suffix.extension</code></pre>
     * 
     * @return The relative path into a repository for the artifact file.
     */
    public String getFileName(String suffix, String extension) {
        StringBuilder file = new StringBuilder();
        file.append(name).append("-").append(version)
            .append(suffix.length() == 0 ? "" : "-").append(suffix)
            .append(".").append(extension);
        return file.toString();
    }

    /**
     * Create the file name for the artifact file with the given file extension.
     * The file name takes the form of following the pattern where the dots in
     * the group are replaced with file part separators.
     * 
     * <code><pre>name-version.extension</code></pre>
     * 
     * @return The relative path into a repository for the artifact file.
     */
    public String getFileName(String extension) {
        return getFileName("", extension);
    }

    /**
     * Create the relative path into a repository for the artifact file with the
     * given suffix and the given file extension. The file name takes the form
     * of following the pattern where the dots in the group are replaced with
     * file part separators.
     * 
     * <code><pre>group/name/version/name-version-suffix.extension</code></pre>
     * 
     * @return The relative path into a repository for the artifact file.
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
     * Create the relative path into a repository for the artifact file with the
     * given file extension. The file name takes the form of following the
     * pattern where the dots in the group are replaced with file part
     * separators.
     * 
     * <code><pre>group/name/version/name-version.extension</code></pre>
     * 
     * @return The relative path into a repository for the artifact file.
     */
    public String getPath(String extension) {
        return getPath("", extension);
    }
    
    public String getDirectoryPath() {
        StringBuilder file = new StringBuilder();
        file.append(group.replace(".", "/"))
                .append("/").append(name)
                .append("/").append(version);
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
    
    private String line(String prefix) {
        StringBuilder line = new StringBuilder();
        line.append(prefix);
        line.append(" ");
        line.append(toString());
        line.append("\n");
        return line.toString();
    }
    
    public String includeLine() {
        return line("+");
    }
    
    public String excludeLine() {
        return line("-");
    }
    
    /**
     * Return the artifact key for this artifact.
     * 
     * @return A string representation of this artifact.
     */
    @Override
    public String toString() {
        return group + "/" + name + "/" + version;
    }
}
