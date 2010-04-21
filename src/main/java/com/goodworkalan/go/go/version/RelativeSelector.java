package com.goodworkalan.go.go.version;

import java.util.regex.Pattern;

/**
 * Selects the greatest version relative to a specific version optoinally
 * constrained by significant version parts.
 * 
 * @author Alan Gutierrez
 */
class RelativeSelector implements SelectionStrategy {
    /** The parts of the version selection pattern. */
    private final int[] parts;

    /**
     * The comparisons to apply at each part, where -1 means less than or equal
     * to, 0 means equal to, and +1 means greater than or equal to. After a
     * non-zero value is encountered, we continue to use the non-zero value
     * and ignore the comparisons array.
     */
    private final int[] comparisons;
    
    /** Matches a valid dot separated version string. */
    private final static Pattern pattern = Pattern.compile("^\\d+(?:\\.\\d+)*$");

    /**
     * Create a relative selector with the given version select
     * <code>pattern</code>. See the class documentation for
     * {@link VersionSelector} for examples of valid relative version select
     * strings.
     * 
     * @param pattern
     *            The selection pattern.
     */
    public RelativeSelector(String pattern) {
        String[] split = pattern.split("\\.");
        int[] comparisons = new int[split.length];
        int[] parts = new int[split.length];
        for (int i = 0; i < split.length; i++) {
            int index = 1;
            if (split[i].charAt(0) == '-') {
                comparisons[i] = -1;
            } else if (split[i].charAt(0) == '+') {
                comparisons[i] = 1;
            } else {
                index = 0;
                comparisons[i] = 0;
            }
            parts[i] = Integer.parseInt(split[i].substring(index));
        }
        this.parts = parts;
        this.comparisons = comparisons;
    }

    /**
     * Return the greatest version number from the list of version numbers that
     * matches the relative version selection pattern or null if none of the
     * version numbers match.
     * <p>
     * Version strings that are not valid dot separated version numbers are
     * ignored and do not match for the purposes of this method.
     * 
     * @param versions
     *            The list of versions.
     * @return The greatest version that matches or null.
     */
    public String select(String... versions) {
        String choice = null;
        int[] best = new int[0];
        for (String option : versions) {
            if (pattern.matcher(option).matches()) {
                String[] split = option.split("\\.");
                int[] version = new int[split.length];
                for (int i = 0, stop = split.length; i < stop; i++) {
                    version[i] = Integer.parseInt(split[i]);
                }
                if (compare(best, version)) {
                    best = version;
                    choice = option;
                }
            }
        }
        return choice;
    }

    /**
     * Get the element at the given <code>index</code> in the given
     * <code>array</code> returning the default value given in
     * <code>outOfBounds</code> if the element does not exist.
     * 
     * @param array
     *            The array.
     * @param index
     *            The array index.
     * @param outOfBounds
     *            The default value to return if the element does not exist.
     * @return The element or the default value if the element does not exist.
     */
    private int get(int[] array, int index, int outOfBounds) {
        return index < array.length ? array[index] : outOfBounds;
    }

    /**
     * Return true of the comparison result given in <code>compare</code> is
     * either equal (zero) or the inequality is the inequality specified for by
     * the comparison index. That is, return true if the element matches the
     * relative comparison specified by the pattern.
     * 
     * @param compare
     *            The result of a comparison.
     * @param comparisonIndex
     *            The index of the affirmative comparison test outcome.
     * @return True if the element matches the relative comparison specified by
     *         the pattern.
     */
    private boolean matched(int compare, int comparisonIndex) {
        return compare == 0 || (compare / Math.abs(compare) == comparisons[comparisonIndex]);
    }

    /**
     * Return true if the given <code>version</code> matches the relative
     * selection pattern and is greater than the given <code>best</code> best
     * version matched so far. The initial best version is an empty array, so
     * that any version that matches will be better than the initial best
     * version.
     * <p>
     * The best version and version given are given as arrays of integer parts.
     * 
     * @param best
     *            The greatest version matched so far.
     * @param version
     *            The version number to test.
     * @return True if the given version matches the pattern and is greater than
     *         the given best version so far.
     */
    public boolean compare(int[] best, int[] version) {
        for (int i = 0; i < parts.length; i++) {
            int compare = get(version, i, 0) - parts[i];
            if ( matched(compare, i)) {
                boolean variable = comparisons[i] != 0;
                if (variable) {
                    int better = get(version, i, 0) - get(best, i, 0);
                    if (better >= 0) {
                        if (compare == 0) {
                            for (int j = i + 1; j < version.length; j++) {
                                if (!matched(version[j] - get(parts, j, 0), i) || version[j] <= get(best, j, 0)) {
                                    return false;
                                }
                            }
                        }
                        return true;
                    }
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }
}
