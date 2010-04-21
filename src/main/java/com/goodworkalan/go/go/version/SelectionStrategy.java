package com.goodworkalan.go.go.version;

/**
 * A strategy for selecting a version number from a list of version numbers.
 * This is the base strategy in a strategy pattern. There are two
 * implementations of this strategy, one that matches a version string exactly,
 * another that matches version numbers relative to a particular version number.
 * 
 * @author Alan Gutierrez
 */
interface SelectionStrategy {
    /**
     * Return a version string selected from the given list of
     * <code>versions</code> or null of none of the versions match the
     * conditions of the selection strategy.
     * 
     * @param versions
     *            The list of versions.
     * @return A version from the list of versions or null.
     */
    public String select(String... versions);
}
