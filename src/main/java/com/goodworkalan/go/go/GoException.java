package com.goodworkalan.go.go;

import com.goodworkalan.cassandra.CassandraException;

public class GoException extends CassandraException {
    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /**
     * Create an exception with the given error code.
     * 
     * @param code
     *            The error code.
     */
    public GoException(int code) {
        super(code);
    }

    /**
     * Create a mix exception with the given error code and cause.
     * 
     * @param code
     *            The error code.
     * @param cause
     *            The wrapped exception.
     */
    public GoException(int code, Throwable cause) {
        super(code, cause);
    }
    
    /** A Task has multiple Task type attributes indicating multiple parents. */
    public final static int MULTIPLE_TASK_PARENTS = 102;
    
    /** A property value ends with a backslash character, does not actually escape anything. */
    public final static int TERMINAL_BACKSLASH = 202;
    
    /** A variable substitution in the property file creates an infinite loop. */
    public final static int PROPERTY_LOOP = 101;
    
    /** A line in an artifact file begins with an invalid character. */
    public final static int INVALID_ARTIFACT_LINE_START = 301;
    
    /** Invalid repository specification line. */
    public final static int INVALID_ARTIFACT_REPOSITORY_LINE = 302;
    
    /** Invalid repository type in repository specification line. */
    public final static int INVALID_ARTIFACT_REPOSITORY_TYPE = 303;

    /** Invalid repository URL in repository specification line. */
    public final static int INVALID_ARTIFACT_REPOSITORY_URL = 304;
    
    /** Cannot find repository constructor. */
    public final static int UNABLE_TO_FIND_REPOSITORY_CONSTRUCTOR = 305;
    
    /** Cannot construct repository. */
    public final static int UNABLE_TO_CONSTRUCT_REPOSITORY = 306;
}
