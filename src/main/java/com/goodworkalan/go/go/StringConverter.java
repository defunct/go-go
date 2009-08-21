package com.goodworkalan.go.go;

/**
 * A no-op converter that converts a string to a string.
 * 
 * @author Alan Gutierrez
 */
public class StringConverter implements Converter {
    /**
     * Convert a string to a string by returning the given string.
     * 
     * @param string
     *            A string to convert.
     * 
     * @return The given string.
     */
    public Object convert(String string) {
        return string;
    }
}
