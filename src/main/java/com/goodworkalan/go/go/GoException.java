package com.goodworkalan.go.go;

import java.util.MissingResourceException;
import java.util.ResourceBundle;


public class GoException extends RuntimeException {
    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    private final int code;

    private final Object[] arguments;

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

    /** Cannot find repository constructor. */
    public final static int REPOSITORY_HAS_NO_URI_CONSTRUCTOR = 305;

    /** Cannot construct repository. */
    public final static int UNABLE_TO_CONSTRUCT_REPOSITORY = 306;

    /** Invalid artifact include specification line. */
    public final static int INVALID_INCLUDE_LINE = 308;

    /** Invalid artifact exclude specification line. */
    public final static int INVALID_EXCLUDE_LINE = 309;

    /** Artifact file not found. */
    public final static int ARTIFACT_FILE_NOT_FOUND = 310;

    /** An I/O exception was thrown while reading an artifact file. */
    public final static int ARTIFACT_FILE_IO_EXCEPTION = 311;
    
    /** An I/O exception was thrown while reading an artifact file. */
    public final static int ARTIFACT_FILE_MISPLACED_EXCLUDE = 312;
    
    /** A repository type is missing an implementation. */
    public final static int MISSING_REPOSITORY_IMPLEMENTATION = 321;

    /** An exception was thrown during task property assignment. */
    public final static int ASSIGNMENT_EXCEPTION_THROWN = 401;

    /** Unable to assign a task property. */
    public final static int ASSIGNMENT_FAILED = 402;

    /** Unable to create task. */
    public final static int CANNOT_CREATE_TASK = 601;
    
    /** Unable to find a string constructor for argument of type. */
    public final static int CANNOT_CREATE_FROM_STRING = 602;
    
    /** Invocation of string constructor on an argument failed. */
    public final static int STRING_CONSTRUCTOR_ERROR = 603;
    
    /** Invocation of string constructor rejected the string data. */
    public final static int STRING_CONVERSION_ERROR = 604;
    
    /** Unable to resolve artifact. */
    public final static int UNRESOLVED_ARTIFACT = 701;
    
    /** Unable to find the responder for a class during installation. */
    public final static int CANNOT_FIND_RESPONDER = 801;
    
    
    /**
     * Create an exception with the given error code.
     * 
     * @param code
     *            The error code.
     * @param arguments
     *            The format arguments.
     */
    public GoException(int code, Object...arguments) {
        this(code, null, arguments);
    }

    /**
     * Create a mix exception with the given error code and cause.
     * 
     * @param code
     *            The error code.
     * @param cause
     *            The wrapped exception.
     * @param arguments
     *            The format arguments.
     */
    public GoException(int code, Throwable cause, Object...arguments) {
        super(null, cause);
        this.code = code;
        this.arguments = arguments;
    }

    /**
     * Get the error code.
     * 
     * @return The error code.
     */
    public int getCode() {
        return code;
    }

    /**
     * Returns the detail message string of this error.
     * 
     * @return The detail message string of this error.
     */
    @Override
    public String getMessage() {
        ResourceBundle bundle = ResourceBundle.getBundle(getClass().getPackage().getName() + ".exceptions");
        String key = Integer.toString(code);
        try {
            return String.format(bundle.getString(key), arguments);
        } catch (MissingResourceException e) {
            return key;
        }
    }
}
