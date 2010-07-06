package com.goodworkalan.go.go.library;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Set;


/**
 * A class path part that references an artifact in repository. 
 *
 * @author Alan Gutierrez
 */
public class ArtifactPart implements PathPart {
    /** The repository directory. */
    private final File libraryDirectory;
    
    /** The artifact. */
    private final Artifact artifact;
    
    /** The set of excludes in effect when this artifact part was expanded. */
    private final Set<Exclude> excludes;
    
    // TODO Document.
    public ArtifactPart(File libraryDirectory, Artifact artifact, Set<Exclude> excludes) {
        this.libraryDirectory = libraryDirectory;
        this.artifact = artifact;
        this.excludes = excludes;
    }

    /**
     * Get the directory of the library where the artifact was found.
     * 
     * @return The library directory.
     */
    public File getLibraryDirectory() {
        return libraryDirectory;
    }

    /**
     * Get the classes jar of the artifact in the library.
     * 
     * @return The file.
     */
    public File getFile() {
        return new File(libraryDirectory, artifact.getPath("jar"));
    }

    /**
     * Get the jar protocol URL for the jar of the artifact in the library. 
     * 
     * @return The URL.
     */
    public URL getURL() {
        return PathParts.toURL("jar:" + PathParts.toURL(getFile().toURI().toString()).toExternalForm() + "!/");
    }

    /**
     * Get the artifact.
     * 
     * @return The artifact.
     */
    public Artifact getArtifact() {
        return artifact;
    }

    /**
     * Because this is already an expanded path part, this expand method returns
     * an collection containing only this path part.
     * 
     * @param library
     *            The library.
     * @param expanded
     *            The list of expended path parts.
     * @param expand
     *            The list of path parts to expand.
     */
    public void expand(Library library, Collection<PathPart> expanded, Collection<PathPart> expand) {
        expanded.add(this);
    }

    /**
     * Return an unversioned key based on the group and name of the contained
     * artifact.
     * 
     * @return An unversioned key.
     */
    public Object getUnversionedKey() {
        return artifact.getUnversionedKey();
    }
    
    /**
     * Return the set of excludes that were in effect when this path part was
     * expanded.
     * 
     * @return The set of excludes in effect when this path part was expanded.
     */
    public Set<Exclude> getExcludes() {
        return excludes;
    }
    
    // TODO Document.
    public String toString() {
        return artifact.toString();
    }
}
