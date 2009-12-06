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

    /**
     * Expands a collection of path parts, turning unsolved path parts into path
     * parts that have a file representation on the file system. Any unresolved
     * path parts are sought in the given library.
     * <p>
     * There are unresolved path parts and resolved path parts. Unresolved path
     * parts are path parts that need to be looked up in the library. Resolved
     * path parts are path parts that are mapped to a file on the file system.
     * <p>
     * This is an application of the prototype design pattern. The unresolved
     * path parts act as prototypes for the resolved path parts. The unresolved
     * path parts in the input collection will add resolved path parts to the
     * output collection. The resolved path parts simply add a copy of themselve
     * <p>
     * Each path part in the output path part collection will be unique
     * according to {@link #getUnversionedKey()}. That is, no two part parts in
     * the output path part collection will have the same value for
     * {@link #getUnversionedKey()}. This means that duplicate entries in the
     * input path part will be eliminated.
     * <p>
     * It also means that versions of dependent resources can be overridden by
     * specifying them in the input path parts. If one of the unresolved path
     * parts has a dependency on <code>group/project/1.1</code> and you want to
     * use <code>group/project/1.2</code> instead, you can add an
     * {@link ResolutionPart} that resolves <code>group/project/1.2</code> to
     * the input path part collection. The path parts for
     * <code>group/project/1.2</code> and <code>group/project/1.1</code> will
     * have the same value for {@link #getUnversionedKey()}. The path part for
     * <code>group/project/1.2</code> will be evaluated before any dependencies
     * and override the path part for <code>group/project/1.2</code> 1.1
     * version.
     * <p>
     * While resolving unresolved path parts, their unresolved dependencies are
     * added When unresolved path parts are resolved, any dependent artifacts in
     * at the end of the list of unresolved path parts to evaluate. Therefore,
     * each unresolved path part in the input path part collection is resolved
     * before any of it's dependencies are resolved. Using this method,
     * dependencies are resolved in levels. The dependencies that are added by
     * the unresolved path parts in the input path part collection are each
     * resolved before any dependencies that they add, and so on.
     * <p>
     * If a path part exists in the output path part collection with the same
     * value of {@link #getUnversionedKey()} as one in the list to evaluate, the
     * path part is not evaluated. It is skipped. This will eliminate duplicates
     * in the input path part collection. This means that we have the
     * opportunity to override the version of an included artifact by explicitly
     * including a specific version in the list of path parts provided to
     * <code>expand</code>.
     * <p>
     * The output path part collection will contain only resolved path parts
     * that have a file representation on the file system that can be used in a
     * classpath. If you pass the output path part collection back to
     * <code>expand()</code> you'll simply create a copy of the path part
     * collection.
     * 
     * @param library
     *            The library.
     * @param expand
     *            The list of path parts to expand.
     * @return An expanded list of path parts that all have a file
     *         representation on the file system.
     */
    public Collection<PathPart> expand(Library library, Collection<PathPart> expand);

    /**
     * A key that identifies the <code>PathPart</code> implementation
     * without versioning that is used to ensure that the first specified
     * version of a jar is the one that is used.
     * 
     * @return A unique, unversioned key for the <code>PathPart</code>.
     */
    public Object getUnversionedKey();
}
