package com.goodworkalan.go.go.library;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Set;


/**
 * A prototype interface for an archive or directory in a file path. Given a
 * list of path parts, a library will expand the list, by calling the the
 * {@link #expand(Library, Collection, Collection) expand} method of the
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
     */
    public URL getURL();

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
     * Expands a collection of path parts, turning unexpanded path parts into
     * expanded path parts, path parts that have a file representation on the
     * file system. Any unexpanded path parts are sought in the given library.
     * <p>
     * There are unexpanded path parts and expanded path parts. Unexpanded path
     * parts are path parts that need to be looked up in the library. Expanded
     * path parts are path parts that are mapped to a file on the file system.
     * <p>
     * This interface employs the prototype design pattern. The unexpanded path
     * parts act as prototypes for the expanded path parts. When a collection of
     * path parts is given to a library for expansion, path parts that are
     * already expanded are left in place, unexpanded path parts are replaced
     * with an expanded path part, and any dependencies added by expanded path
     * parts are appended to the class path.
     * <p>
     * An unexpanded path part can request unexpanded dependencies, so those
     * unexpanded dependencies added to the end of the class path will also be
     * expanded until all of the path parts are expanded path parts.
     * <p>
     * Each path part in the output path part collection will be unique
     * according to the {@link #getUnversionedKey() getUnversionedKey} property.
     * That is, no two part parts in the output path part collection will have
     * the same value for the {@link #getUnversionedKey() getUnversionedKey}
     * method. This means that duplicate entries in the input path part will be
     * eliminated.
     * <p>
     * It also means that versions of dependent resources can be overridden by
     * specifying them in the input path parts. If one of the unexpanded path
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
     * While resolving unexpanded path parts, their unexpanded dependencies are
     * added When unexpanded path parts are expanded, any dependent artifacts in
     * at the end of the list of unexpanded path parts to evaluate. Therefore,
     * each unexpanded path part in the input path part collection is expanded
     * before any of it's dependencies are expanded. Using this method,
     * dependencies are expanded in levels. The dependencies that are added by
     * the unexpanded path parts in the input path part collection are each
     * expanded before any dependencies that they add, and so on.
     * <p>
     * If a path part exists in the output path part collection with the same
     * value of {@link #getUnversionedKey()} as one in the list to evaluate, the
     * path part is not evaluated. It is skipped. This will eliminate duplicates
     * in the input path part collection. This means that we have the
     * opportunity to override the version of an included artifact by explicitly
     * including a specific version in the list of path parts provided to
     * <code>expand</code>.
     * <p>
     * The output path part collection will contain only expanded path parts
     * that have a file representation on the file system that can be used in a
     * classpath. If you pass the output path part collection back to
     * <code>expand()</code> you'll simply create a copy of the path part
     * collection.
     * 
     * @param library
     *            The library.
     * @param expanded
     *            The list of expended path parts.
     * @param expand
     *            The list of path parts to expand.
     */
    public void expand(Library library, Collection<PathPart> expanded, Collection<PathPart> expand);

    /**
     * A key that identifies the <code>PathPart</code> implementation without
     * versioning that is used to ensure that the first specified version of a
     * jar is the one that is used.
     * 
     * @return A unique, unversioned key for the <code>PathPart</code>.
     */
    public Object getUnversionedKey();

    /**
     * Return the set of excludes that were in effect when this path part was
     * expanded.
     * 
     * @return The set of excludes in effect when this path part was expanded.
     */
    public Set<Exclude> getExcludes();
}
