package com.goodworkalan.go.go;

import java.net.URI;

/**
 * Describes a repository as specified in an artifacts file or a bundle
 * specification.
 * 
 * @author Alan Gutierrez
 */
public class Repository {
    /** The repository type, an identifier string. */
    public final String type;
    
    /** The repository URI. */
    public final URI uri;

    /**
     * Create a repository structure of the given repository type with the given
     * repository URL.
     * 
     * @param type
     *            The repository type.
     * @param uri
     *            The repository URL.
     */
    public Repository(String type, URI uri) {
        if (type.length() == 0) {
            throw new GoException(0);
        }
        if (!Character.isJavaIdentifierStart(type.charAt(0))) {
            throw new GoException(0);
        }
        for (int i = 1, stop = type.length(); i < stop; i++) {
            if (!Character.isJavaIdentifierPart(type.charAt(i))) {
                throw new GoException(0);
            }
        }
        this.type = type;
        this.uri = uri;
    }    
}
