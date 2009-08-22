package com.goodworkalan.go.go;
import static com.goodworkalan.go.go.GoException.*;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.naming.InitialContext;
import javax.naming.NamingException;

public class VariableProperties {
    /** The serial version id. */
    private static final long serialVersionUID = 1L;
    
    /** The underlying properties hash table. */
    private final Properties properties;

    /**
     * Create a variable properties lookup with the given properties hash table.
     * 
     * @param properties
     *            The properties.
     */
    VariableProperties(Properties properties) {
        this.properties = properties;
    }

    /**
     * Return the character value for the escaped character.
     * 
     * @param ch
     *            A character immediately following a backslash.
     * @return The unescaped value for the backslash escape sequence.
     */
    private final char unescape(char ch)
    {
        switch (ch) {
        case 'n': return '\n';
        case 'r': return '\r';
        case 't': return '\t';
        case 'f': return '\f';
        case 'b': return '\b';
        }
        return ch;
    }
    
    public String getValue(String value) {
        return expandValue(value, new HashSet<String>());
    }

    /**
     * Return a property from a configuration file with variables substituted or
     * null if no such property exists.
     * 
     * @param name
     *            The property name.
     * @return The value associated with the property with variables replaced or
     *         null if none exists.
     */
    public String getProperty(String name) {
        return expand(name, new HashSet<String>());
    }

    /**
     * Return a property from a configuration file with variables substituted or
     * else return the given default property if no such property exists.
     * 
     * @param name
     *            The property name.
     * @param defaultValue
     *            The default value to return if no such property exists.
     * @return The value associated with the property with variables replaced or
     *         null if none exists.
     */
    public String getProperty(String name, String defaultValue) {
        String value = expand(name, new HashSet<String>());
        return value == null ? defaultValue : value;
    }

    /**
     * Get the value for the key from the hash table, substituting any variables
     * found in the value, making sure not to enter an infinite look by not
     * dereferencing a key that has already been seen.
     * 
     * @param name
     *            The key to dereference.
     * @param seen
     *            Keysh that have already been dereferenced.
     * @return The expanded value or null if none exists for the given key.
     */
    private String expand(String name, Set<String> seen) {
        if (seen.contains(name)) {
            throw new GoException(PROPERTY_LOOP).add(name);
        }
        seen.add(name);

        // Optimize last (i.e. never).

        String value = properties.getProperty(name);
        if (value != null) {
            try {
                expandValue(value, seen);
            } catch (GoException e) {
                e.add(name);
                throw e;
            }
        }
        return value;
    }

    private String expandValue(String value, Set<String> seen) {
        boolean copying = true;
        StringBuilder variable = new StringBuilder();
        StringBuilder expanded = new StringBuilder();
        for (int i = 0, size = value.length(); i < size; i++) {
            char ch = value.charAt(i);
            switch (value.charAt(i)) {
            case '\\':
                if (++i < size) {
                    expanded.append(unescape(value.charAt(i)));
                } else {
                    throw new GoException(TERMINAL_BACKSLASH);
                }
                break;
            case '$':
                if (i + 1 < size && value.charAt(i + 1) == '{') {
                    copying = false;
                    i = i + 1;
                } else {
                    expanded.append(ch);
                }
                break;
            case '}':
                if (copying) {
                    expanded.append(ch);
                } else {
                    String subName = variable.toString();
                    String replace = System.getProperty(subName);
                    if (replace == null) {
                        replace = expand(subName, seen);
                    }
                    if (replace != null) {
                        expanded.append(replace);
                    }
                    copying = true;
                }
                break;
            default:
                if (copying) {
                    expanded.append(ch);
                } else {
                    variable.append(ch);
                }
            }
        }
        return expanded.toString();
    }
}
