package com.goodworkalan.go.go;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

/**
 * A prototype interface for an archive or directory in a file path. Given a
 * list of path parts, a library will expand the list, by calling the the
 * {@link #expand(Library, Collection) expand} method of the
 * <code>PathPart</code>. A prototype <code>PathPart</code> implementation may
 * append additional <code>PathPart</code> instances to the path, if it is an
 * {@link Artifact}, for example, that has dependencies.
 * <p>
 * To create a path you first build a collection of <code>PathPart</code>
 * objects. Then you pass that collection to
 * {@link Library#resolve(Collection, java.util.Set) Library.resolve}. The
 * result is an expanded collection of <code>PathPart</code> instances, where
 * all prototypes instances have been replaced by instances that represent an
 * actual directory in the file system.
 * <p>
 * After expanding a path using the
 * {@link Library#resolve(Collection, java.util.Set) Library.resolve} method all
 * of the path parts will return a value when their {@link #getFile() getFile}
 * or {@link #getURL() getURL} attribute accessors are invoked. Prototype
 * <code>PathPart</code> implementations thrown an
 * <code>UnsupportedOperationException</code> when any of the
 * <code>PathPart</code> attribute accessors are invoked.
 * 
 * @author Alan Gutierrez
 */
// FIXME No one needs to worry about key.
// FIXME Here is your new documentation style.
// FIXME Make this a class.
// FIXME Rename directory.
// FIXME Maybe PathExpander as a builder?
public interface PathPart {
    /**
     * Get the directory or archive file. If this is a prototype <code>PathPart</code>
     * implementation, an <code>UnsupportedOperationException</code> is thrown.
     * 
     * @return The directory or archive file.
     * @exception UnsupportedOperationException
     *                If this is a prototype <code>PathPart</code>
     *                implementation.
     */
    public File getFile();

    /**
     * Get the URL for the directory or archive file. If this is a prototype
     * <code>PathPart</code> implementation, an
     * <code>UnsupportedOperationException</code> is thrown.
     * 
     * @return The URL for the directory or archive file.
     * @throws MalformedURLException
     *             Probably never, since the file is converted first to a
     *             <code>URI</code> object before it is converted to a
     *             <code>URL</code> object, you'll get a
     *             <code>URISyntaxException</code> first.
     */
    public URL getURL() throws MalformedURLException;

    /**
     * Get the directory artifact or null if the directory was not obtained by
     * locating an artifact in a library. This method will return null for
     * <code>PathPart</code> implementations that wrap a specific JAR or classes
     * directory.
     * 
     * @return The artifact or null.
     */
    public Artifact getArtifact();

    public Collection<PathPart> expand(Library library, Collection<PathPart> expand);

    /**
     * A key that uniquely identifies the <code>PathPart</code> implementation
     * that is used to ensure that directories are not repeated in the path.
     * 
     * @return A unique key for the <code>PathPart</code>.
     */
    public Object getKey();
}
