package com.goodworkalan.go.go.version;

/**
 * Selects the version from a list of version that matches a particular version
 * exactly.
 * 
 * @author Alan Gutierrez
 */
class ExactSelector implements SelectionStrategy {
    /** The version to match. */
    private final String version;

    /**
     * Create a version selector that will match the given version exactly.
     * 
     * @param version
     */
    public ExactSelector(String version) {
        this.version = version;
    }

    /**
     * Return the version from the given list of <code>versions</code> that
     * exactly matches the version used to construct this version selector or
     * null if the version does not exist in the list.
     * 
     * @param versions
     *            The list of versions.
     * @return The exact version matched by this version selector or null.
     */
    public String select(String... versions) {
        for (String option : versions) {
            if (option.equals(version)) {
                return option;
            }
        }
        return null;
    }
}
