package com.goodworkalan.go.go;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * An extension of list designed to contain verbose argument specifications that
 * provides operations for adding, replacing, and removing the list items based
 * on their qualified argument name.
 * 
 * @author Alan Gutierrez
 */
public class ArgumentList extends ArrayList<String> {
    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /**
     * Create an empty argument list.
     */
    public ArgumentList() {
    }

    /**
     * Create an argument list that is a copy of the given list.
     * 
     * @param list
     *            The list to copy.
     */
    public ArgumentList(List<String> list) {
        super(list);
    }

    /**
     * Add an argument to the list.
     * 
     * @param name
     *            The qualified argument name.
     * @param value
     *            The argument value
     */ 
    public void addArgument(String name, String value) {
        add("--" + name + "=" + value.toString());
    }

    /**
     * Remove the first argument with the given qualified name. Returns the
     * value of the removed argument or null if the argument did not exist.
     * 
     * @param name
     *            The qualified argument name.
     * @return The value of the removed argument or null if the argument did not
     *         exist.
     */
    public String removeArgument(String name) {
        Iterator<String> arguments = iterator();
        while (arguments.hasNext()) {
            String argument = arguments.next();
            String[] pair = argument.substring(2).split("=");
            if (pair[0].equals(name)) {
                arguments.remove();
                return pair[1];
            }
        }
        return null;
    }
    
    public String replaceArgument(String name, String value) {
        for (int i = 0, stop = size(); i < stop; i++) {
            String argument = get(i);
            String[] pair = argument.substring(2).split("=");
            if (pair[0].equals(name)) {
                set(i, "--" + name + "=" + value);
                return pair[1];
            }
        }
        addArgument(name, value);
        return null;
    }

    /**
     * Get the first argument with the given qualified name. Returns the value
     * of the argument or null if the argument did not exist.
     * 
     * @param name
     *            The qualified argument name.
     * @return The value of the argument or null if the argument did not exist.
     */
    public String getArgument(String name) {
        Iterator<String> arguments = iterator();
        while (arguments.hasNext()) {
            String argument = arguments.next();
            String[] pair = argument.substring(2).split("=");
            if (pair[0].equals(name)) {
                return pair[1];
            }
        }
        return null;
    }
}
