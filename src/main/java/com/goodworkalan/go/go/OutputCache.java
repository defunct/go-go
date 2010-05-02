package com.goodworkalan.go.go;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An element in a stack of output caches where a new element is created for
 * each time an executor launches a new thread to accommodate a new class
 * loader, so that output cached in descendant threads is discarded.
 * <p>
 * This prevents us from referencing an object loaded by a child class loader as
 * well as prevents us from leaking memory by holding onto objects of defunct
 * class loaders which will in turn hold onto the defunct class loader.
 * 
 * @author Alan Gutierrez
 */
public class OutputCache {
    /** A map of commands and their arguments to a list of their outputs. */ 
    private final Map<List<List<String>>, List<Object>> cache = new HashMap<List<List<String>>, List<Object>>();

    /** The parent output cache in the stack of output caches. */
    private final OutputCache parent;

    /**
     * Create an output stack element with given parent element. If the parent
     * element is null, this is the first element in the stack.
     * 
     * @param parent
     *            The parent output cache stack element.
     */
    public OutputCache(OutputCache parent) {
        this.parent = parent;
    }

    /**
     * Get the output list for the given command key. The output cache is sought
     * by looking first in the cache of the current stack element, then
     * iterating up the stack checking the caches of the ancestor stack
     * elements. When the key is found in the cache of stack element, the output
     * list is returned and the search ends.
     * 
     * @param key
     *            The command key.
     * @return The output list for the command key.
     */
    public List<Object> getCache(List<List<String>> key) {
        OutputCache iterator = this;
        while (iterator != null) {
            if (iterator.cache.containsKey(key)) {
                return iterator.cache.get(key);
            }
            iterator = iterator.parent;
        }
        return null;
    }
}
