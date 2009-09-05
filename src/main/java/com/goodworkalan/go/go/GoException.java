package com.goodworkalan.go.go;

import com.goodworkalan.cassandra.CassandraException;
import com.goodworkalan.cassandra.Report;

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
        super(code, new Report());
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
        super(code, new Report(), cause);
    }

    /**
     * Create an exception with the given error code and the given initial
     * report structure.
     * 
     * @param code
     *            The error code.
     * @param report
     *            An initial report structure.
     */
    public GoException(int code, Report report) {
        super(code, report);
    }

    /**
     * Create an exception with the given error code and the given initial
     * report structure that wraps the given cause exception.
     * 
     * @param The
     *            error code.
     * @param report
     *            An initial report structure.
     * @param The
     *            cause.
     */
    public GoException(int code, Report report, Throwable cause) {
        super(code, report, cause);
    }

    /** A Task has multiple Task type attributes indicating multiple parents. */
    public final static int MULTIPLE_TASK_PARENTS = 102;

    /**
     * A property value ends with a backslash character, does not actually
     * escape anything.
     */
    public final static int TERMINAL_BACKSLASH = 202;

    /** A variable substitution in the property file creates an infinite loop. */
    public final static int PROPERTY_LOOP = 101;

    /** A line in an artifact file begins with an invalid character. */
    public final static int INVALID_ARTIFACTS_LINE_START = 301;

    /** Invalid repository specification line. */
    public final static int INVALID_REPOSITORY_LINE = 302;

    /** Invalid repository URL in repository specification line. */
    public final static int INVALID_REPOSITORY_URL = 304;

    /** Cannot find repository constructor. */
    public final static int REPOSITORY_HAS_NO_URI_CONSTRUCTOR = 305;

    /** Cannot construct repository. */
    public final static int UNABLE_TO_CONSTRUCT_REPOSITORY = 306;

    /** A repository URL cannot be relative. */
    public final static int RELATIVE_REPOSITORY_URL = 307;

    /** Invalid artifact include specification line. */
    public final static int INVALID_INCLUDE_LINE = 308;

    /** Invalid artifact exclude specification line. */
    public final static int INVALID_EXCLUDE_LINE = 309;

    /** Artifact file not found. */
    public final static int ARTIFACT_FILE_NOT_FOUND = 310;

    /** An I/O exception was thrown while reading an artifact file. */
    public final static int ARTIFACT_FILE_IO_EXCEPTION = 311;
    
    /** A repository type is missing an implementation. */
    public final static int MISSING_REPOSITORY_IMPLEMENTATION = 321;

    /** An exception was thrown during task property assignment. */
    public final static int ASSIGNMENT_EXCEPTION_THROWN = 401;

    /** Unable to assign a task property. */
    public final static int ASSIGNMENT_FAILED = 402;

    /** Unable to create an XML parser to read a POM. */
    public final static int CANNOT_CREATE_XML_PARSER = 501;

    /** POM file not found. */
    public final static int POM_FILE_NOT_FOUND = 502;

    /** An I/O exception was thrown while reading a POM file. */
    public final static int POM_IO_EXCEPTION = 503;

    /** Unable to parse the XML in a POM file. */
    public final static int POM_SAX_EXCEPTION = 504;
    
    /** Unable to create task. */
    public final static int CANNOT_CREATE_TASK = 601;
    
    /** Unable to find a string constructor for argument of type. */
    public final static int CANNOT_CREATE_FROM_STRING = 602;
    
    /** Invocation of string constructor on an argument failed. */
    public final static int STRING_CONSTRUCTOR_ERROR = 603;
    
    /** Invocation of string constructor rejected the string data. */
    public final static int STRING_CONVERSION_ERROR = 604;
}
