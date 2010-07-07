package com.goodworkalan.go.go;

/**
 * FIXME We want to be able to have an exception, so that a command line switch
 * will turn errors into stack traces.
 * 
 * @author Alan Gutierrez
 */
public class GoError extends GoException implements Erroneous {
    /** Serial version id. */
    private static final long serialVersionUID = 1L;
    
    /** No arguments in the command line. */
    public static int COMMAND_LINE_NO_ARGUMENTS = 1001;

    /**
     * Cannot create the directory for a boot configuration file during an
     * install.
     */
    public static int CANNOT_CREATE_BOOT_CONFIGURATION_DIRECTORY = 1002;

    /** Cannot write the boot configuration file during an install. */
    public static int CANNOT_WRITE_BOOT_CONFIGURATION = 1003;
    
    /** Invalid system property definition parameter. */
    public static int INVALID_DEFINE_PARAMETER = 1004;
    
    /** Unable to find the specified commandable class. */
    public static int CANNOT_FIND_COMMANDABLE = 1006;
    
    /** Invalid argument to bootstrap. */
    public static int INVALID_ARGUMENT = 1005;
    
    /** Cannot read a jar archive. */
    public static int CANNOT_READ_JAR_ARCHIVE = 1007;

    /**
     * Create a go error with the given error code.
     * 
     * @param code
     *            The error code.
     * @param arguments
     *            The format arguments.
     */
    public GoError(int code, Object...arguments) {
        super(code, arguments);
    }

    /**
     * Create a go error with the given error code and the given cause.
     * 
     * @param code
     *            The error code.
     * @param cause
     *            The cause.
     * @param arguments
     *            The format arguments.
     */
    public GoError(int code, Throwable cause, Object...arguments) {
        super(code, cause, arguments);
    }
    
    /**
     * The exit code.
     * 
     * @return The exit code.
     */
    public int getExitCode() {
        return 1;
    }
}
