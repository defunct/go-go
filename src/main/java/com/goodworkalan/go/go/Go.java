package com.goodworkalan.go.go;

import java.io.File;
import java.util.List;

/**
 * Static methods for the Go application.
 * 
 * @author Alan Gutierrez
 */
public class Go {
    /** The command verbosity. */
    static int verbosity = 1;

    /**
     * Run a Jav-a-Go-Go program specified by the given arguments that will draw
     * dependences from the given library path.
     * 
     * @param libraries
     *            The libraries.
     * @param arguments
     *            The command line arguments.
     */
    public static void main(List<File> libraries, String...arguments) {
        System.exit(new ProgramQueue(libraries, arguments).start());
    }
}
