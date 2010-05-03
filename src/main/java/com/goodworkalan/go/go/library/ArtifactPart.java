package com.goodworkalan.go.go.library;

import java.io.File;
import java.net.URL;
import java.util.Collection;


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
    
    public ArtifactPart(File libraryDirectory, Artifact artifact) {
        this.libraryDirectory = libraryDirectory;
        this.artifact = artifact;
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
     *            The library to use to resolve artifact includes.
     * @param expand
     *            A list of dependent path parts to add to the expanded class
     *            path.
     * @return A collection containing only this path part.
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
}
