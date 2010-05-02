package com.goodworkalan.go.go;

import java.io.File;
import java.net.URL;

/**
 * Implements the methods that expanding path parts do not implement as
 * exception throwing unimplemented methods.
 * <p>
 * The path part is a prototype pattern, where dependency specifications set in
 * an unexpanded path along with actual class path elements. An expanded path
 * consists entirely of actual class path elements. An unexpanded class path has
 * place holder elements which do not have a reasonable answer when asked for
 * their files or URLs. This implementation gives very unreasonable answers.
 * 
 * @author Alan Gutierrez
 */
public abstract class ExpandingPathPart implements PathPart {
    /**
     * Throws an exception indicating that the method is not implemented.
     * 
     * @return Nothing.
     * @exception UnsupportedOperationException
     *                If called.
     */
    public Object getUnversionedKey() {
        throw new UnsupportedOperationException();
    }

    /**
     * Throws an exception indicating that the method is not implemented.
     * 
     * @return Nothing.
     * @exception UnsupportedOperationException
     *                If called.
     */
    public Artifact getArtifact() {
        throw new UnsupportedOperationException();
    }

    /**
     * Throws an exception indicating that the method is not implemented.
     * 
     * @return Nothing.
     * @exception UnsupportedOperationException
     *                If called.
     */
    public File getFile() {
        throw new UnsupportedOperationException();
    }

    /**
     * Throws an exception indicating that the method is not implemented.
     * 
     * @return Nothing.
     * @exception UnsupportedOperationException
     *                If called.
     */
    public URL getURL() {
        throw new UnsupportedOperationException();
    }
}
