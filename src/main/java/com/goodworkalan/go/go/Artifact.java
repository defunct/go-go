package com.goodworkalan.go.go;


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
     * Return the artfact key for this artifact.
     * 
     * @return A string representation of this artifact.
     */
    @Override
    public String toString() {
        return getKey() + "/" + version;
    }
}
