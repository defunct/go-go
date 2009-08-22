package com.goodworkalan.go.go;


/**
 * A file needed for the build, usually a jar file.
 *
 * @author Alan Gutierrez
 */
public class Artifact {
    /** The dependency group. */
    private final String group;

    /** The dependency name. */
    private final String name;

    /** The dependency version. */
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
     * Get a key that uniquely defines the artifact without version.
     * 
     * @return A key.
     */
    public String getKey() {
        return group + "/" + name;
    }

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
}
