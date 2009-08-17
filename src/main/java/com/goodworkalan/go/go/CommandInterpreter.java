package com.goodworkalan.go.go;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Make sense of the command line.
 * 
 * @author Alan Gutierrez
 */
public class CommandInterpreter {
    public void main(String[] arguments) {
        Map<String, String> parameters = new LinkedHashMap<String, String>();
        for (int i = 0, stop = arguments.length; i < stop; i++) {
            System.out.println(arguments[i]);
        }
    }

    /**
     * Invoke the parsed command line.
     * 
     * @param command
     *            The parsed command line.
     */
    public void main(CommandLine commandLine) {
    }
}
