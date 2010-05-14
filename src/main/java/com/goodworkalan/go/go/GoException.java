package com.goodworkalan.go.go;

import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.goodworkalan.danger.CodedDanger;

/**
 * A general purpose exception that indicates that an error occurred in one 
 * of the classes in the go package.
 *   
 * @author Alan Gutierrez
 */
public class GoException extends CodedDanger {
    /** The static cache of exception message resource bundles. */
    private final static ConcurrentMap<String, ResourceBundle> BUNDLES = new ConcurrentHashMap<String, ResourceBundle>();

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /** A Task has multiple Task type attributes indicating multiple parents. */
    // FIXME REname.
    public final static int MULTIPLE_TASK_PARENTS = 102;

    /** Unable to enumerate the available command resource files. */
    public final static int COMMANDABLE_RESOURCES_IO = 104;

    /** Unable to read the command definition resource file. */
    public final static int COMMANDABLE_RESOURCE_IO = 105;

    /**
     * A property value ends with a backslash character, does not actually
     * escape anything.
     */
    public final static int TERMINAL_BACKSLASH = 202;

    /** A line in an artifact file begins with an invalid character. */
    public final static int INVALID_ARTIFACTS_LINE_START = 301;

    /** Cannot find repository constructor. */
    public final static int REPOSITORY_HAS_NO_URI_CONSTRUCTOR = 305;

    /** Cannot construct repository. */
    public final static int UNABLE_TO_CONSTRUCT_REPOSITORY = 306;

    /** Invalid artifact include specification line. */
    public final static int INVALID_INCLUDE_LINE = 308;

    /** Invalid artifact exclude specification line. */
    public final static int INVALID_INCLUDE = 309;

    /** Artifact file not found. */
    public final static int ARTIFACT_FILE_NOT_FOUND = 310;

    /** An I/O exception was thrown while reading an artifact file. */
    public final static int ARTIFACT_FILE_IO_EXCEPTION = 311;
    
    /** An I/O exception was thrown while reading an artifact file. */
    public final static int ARTIFACT_FILE_MISPLACED_EXCLUDE = 312;
    
    /** A repository type is missing an implementation. */
    public final static int MISSING_REPOSITORY_IMPLEMENTATION = 321;
    
    /** Invalid exclude string. */
    public final static int INVALID_EXCLUDE = 322;
    
    /** Attempted to parse malformed URL. */
    public final static int MALFORMED_URL = 323;
    
    /** Attempted to parse malformed artifact string. */
    public final static int MALFORMED_ARTIFACT = 324;
    
    /** Attempted to parse malformed artifact file. */
    public final static int MALFORMED_ARTIFACT_FILE = 325;

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
    
    /** An exception occurred while executing a future. */
    public final static int FUTURE_EXECUTION = 605;
    
    /** Command exited by throwing exit code. */
    public final static int EXIT = 606;
    
    /** Unable to resolve artifact. */
    public final static int UNRESOLVED_ARTIFACT = 701;
    
    /** Command class missing. */
    public final static int COMMAND_CLASS_MISSING = 101;
    
    /**
     * Create an exception with the given error code.
     * 
     * @param code
     *            The error code.
     * @param arguments
     *            The format arguments.
     */
    public GoException(int code, Object...arguments) {
        super(BUNDLES, code, null, arguments);
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
        super(BUNDLES, code, cause, arguments);
    }

    public static int erroneous(InputOutput io, int verbosity, Throwable e) {
        if (verbosity > 0) {
            e.printStackTrace(io.err);
        } else {
            io.err.println(e.getMessage());
        }
        return ((Erroneous) e).getExitCode();
    }

    // FIXME Why static?
    public int unwrap(InputOutput io, int verbosity) {
        Throwable iterator = this;
        while ((iterator instanceof GoException) && ((GoException) iterator).getCode() == FUTURE_EXECUTION) {
            iterator = iterator.getCause().getCause();
        }
        if ((iterator instanceof GoException) && ((GoException) iterator).getCode() == EXIT) {
            return ((Exit) iterator.getCause()).code;
        }
        if (iterator instanceof Erroneous) {
            return erroneous(io, verbosity, iterator);
        }
        throw this;
    }
}
