package com.goodworkalan.go.go;

/**
 * Converts strings into objects for use in tasks.
 *
 * @author Alan Gutierrez
 */
public interface Converter {
    /**
     * Convert the given string into an object.
     * 
     * @param string
     *            The string.
     * @return An object created from the string.
     */
    public Object convert(String string);
}
