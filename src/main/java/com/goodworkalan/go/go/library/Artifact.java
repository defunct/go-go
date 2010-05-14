package com.goodworkalan.go.go.library;

import static com.goodworkalan.go.go.GoException.MALFORMED_ARTIFACT;
import static com.goodworkalan.go.go.GoException.MALFORMED_ARTIFACT_FILE;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.goodworkalan.go.go.GoException;

/**
 * A jar artifact needed for execution.
 * <p>
 * This class describes a single jar library artifact. The artifacts are storied
 * in a Maven 2 formatted repository. An artifact represents and entry in that
 * repository. The <code>Artifact</code> class obtains artifacts
 * <p>
 * Classifiers are nipples, extra nipples, that create an odd number of nipples,
 * 3 nipples or 5 nipples, more than the expected number, never an even number,
 * that seems to be the Maven way, to had just one too many nipples.
 * <p>
 * As such, classifiers are buried in this class for the support of odd nippled
 * projects that use classifiers. Java really ought not have multiple binary
 * outputs, and when you do, you could build specify different outputs with
 * suffixes or incredibly different builds with two checkouts of the same
 * project. Please don't benipple your artifacts on our account.
 * <p>
 * An artifact is meant to describe a single jar library and it's relevant
 * files, but a classifer makes an artifact a container, or a branch, with
 * multiple binary files, but still one set of supporting files. As rarely as a
 * classifer is used, this makes for a strange appendages, people are not going
 * to expect this. You certainly can't use both a JDK 1.5 and JDK 1.4 version of
 * the same library as a dependency, it's either one or the other, and you can
 * easily specify one or the other using a project name.
 * <p>
 * The group answers, "who?" The name answers, "what?" The version answers,
 * "when?"
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
     * @param excludes
     *            The list of excludes.
     */
    public Artifact(String group, String name, String version) {
        this.group = group;
        this.name = name;
        this.version = version;
    }

    /**
     * Create an artifact from an artifact specification string.
     * <p>
     * An artifact specification string takes the form
     * <code>group/name/version/classifier</code>.
     * <p>
     * The version is optional and the classifier is an annoyance, that is also
     * very optional.
     * 
     * @param artifact
     *            The artifact string.
     */
    public Artifact(String artifact) {
        List<String> parts = new ArrayList<String>(Arrays.asList(artifact.split("/")));
        if (parts.size() == 2) {
            parts.add("+0");
        }
        if (parts.size() != 3) {
            // This is a go exception because it may have come from outside,
            // but suffix is an IllegalArgumentException because it is most
            // likely a programming error.
            throw new GoException(MALFORMED_ARTIFACT, artifact);
        }
        this.group = parts.get(0);
        this.name = parts.get(1);
        this.version = parts.get(2);
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
    public Artifact(File file) {
        LinkedList<File> parts = new LinkedList<File>();
        File directory = file.getParentFile();
        while (directory != null) {
            parts.addFirst(directory);
            directory = directory.getParentFile();
        }
        if (parts.size() < 3) {
            throw new GoException(MALFORMED_ARTIFACT_FILE, file);
        }
        String version = parts.removeLast().getName();
        String name = parts.removeLast().getName();
        StringBuilder group = new StringBuilder();
        String separator = "";
        for (File part : parts) {
            group.append(separator).append(part.getName());
            separator = ".";
        }
        this.group = group.toString();
        this.name = name;
        this.version =  version;
    }

    /**
     * Get a key that is a list that uniquely defines the artifact.
     * 
     * @return A key as a list of name, group, version.
     */
    public List<String> getKey() {
        List<String> key =  new ArrayList<String>();
        key.add(group);
        key.add(name);
        key.add(version);
        return key;
    }

    /**
     * Get a key that uniquely defines the artifact project name without version
     * of classifier.
     * 
     * @return The unversioned key.
     */
    public List<String> getUnversionedKey() {
        return new Exclude(getKey().subList(0, 2));
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
     * Convert a suffix from slash delimited to dot delimited. Slash delimited
     * suffixes are a way to denote the file extension part from the extended
     * file name part. This means that you can specify
     * "project-1.0-sources.tar.gz" by providing the suffix "sources/tar.gz".
     * 
     * @param suffix
     *            The file suffix pattern.
     * @return The file suffix converted to a file name ending and file
     *         extension suffix for appending to a file name.
     */
    public static String suffix(String suffix) {
        String[] split = suffix.split("/");
        switch (split.length) {
        case 1:
            return "." + suffix;
        case 2:
            return "-" + split[0] + "." + split[1];
        default:
            throw new IllegalArgumentException();
        }
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
    public String getFileName(String suffix) {
        StringBuilder file = new StringBuilder();
        file.append(name).append("-").append(version).append(suffix(suffix));
        return file.toString();
    }

    /**
     * Create the relative path into a repository for the artifact file with the
     * given suffix and the given file extension. The file name takes the form
     * of following the pattern where the dots in the group are replaced with
     * file part separators.
     * 
     * <code><pre>group/name/version/name-version-suffix.extension</pre></code>
     * 
     * <p>
     * FIXME Use {@link File#separator}.
     * 
     * @return The relative path into a repository for the artifact file.
     */
    public String getPath(String suffix) {
        StringBuilder file = new StringBuilder();
        file.append(group.replace(".", "/"))
                .append("/").append(name)
                .append("/").append(version)
                .append("/")
                    .append(name).append("-").append(version).append(suffix(suffix));
        return file.toString();
    }

    /**
     * Create the relative path into a repository for the artifact directory.
     * The directory named takes for form of the following where the dots in the
     * group are replaced with file part separators.
     * 
     * @return The relative path into a repository of the artifact directory.
     */
    public String getDirectoryPath() {
        StringBuilder file = new StringBuilder();
        file.append(group.replace(".", "/"))
                .append("/").append(name)
                .append("/").append(version);
        return file.toString();
    }
    
    /**
     * Create the relative path into a repository for the artifact directory without .
     * The directory named takes for form of the following where the dots in the
     * group are replaced with file part separators.
     * 
     * @return The relative path into a repository of the artifact directory.
     */
    public String getUnversionedDirectoryPath() {
        StringBuilder file = new StringBuilder();
        file.append(group.replace(".", "/"))
                .append("/").append(name);
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
     * Generate a dependency file line that will include this artifact.
     * 
     * @return A dependency file line,
     */
    public String getArtifactsFileLine(Set<Exclude> excludes) {
        return new Include(this, excludes).getArtifactFileLine();
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
