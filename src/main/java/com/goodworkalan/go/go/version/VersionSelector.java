package com.goodworkalan.go.go.version;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Selects a version number relative to another version number specified on a
 * version selection pattern string. Relative selection only works with versions
 * represented by dot separated integer values, but does work for version
 * numbers with any number of parts.
 * <p>
 * For an exact pattern or for a pattern that does not match a dot separated
 * integer version number, the version selector will select a version string
 * that is an exact match.
 * <p>
 * For a relative pattern the version selector will select the greatest version
 * number that matches relative pattern. When a version number is compared
 * against a version number with more parts, missing integer parts are
 * interpreted as zero so that 3.2.1 is greater than 3.2.
 * <p>
 * A relative pattern is indicated by placing either a '+' or a '-' before the
 * integer in one of the integer parts.
 * <p>
 * A '+' means that the pattern will match any version where the signed part and
 * any of parts of lesser significance are greater than or equal to the signed
 * part in the pattern, and where the parts of greater significance are equal to
 * the parts in the pattern.
 * <p>
 * For example, the pattern 3.+2.2 matches 3.2.2, 3.2.3 and 3.3, but does not
 * match 3.2.1, 3.1, 2 or 4. The most significant part is fixed. In this way,
 * you can say that you're willing to accept any version of a specific major
 * version, in this case version 3, that is greater than a specific minor and
 * micro version, in this case 3.2.2. In other words, match any version 3
 * release that is greater than 3.2.2.
 * <p>
 * To match any version at all greater than 3.2.2, regardless of major version,
 * one would apply the sign to the most significant version part; +3.2.2.
 * <p>
 * A '-' means that the pattern will match any version where the signed part and
 * any of parts of lesser significance are less than or equal to than the signed
 * part, and where the parts of greater significance are equal to the parts in
 * the pattern.
 * <p>
 * For example, the pattern 3.-2.2 matches 3.2.2, 3.2.1, 3.1 and 3, but does not
 * match 3.2.3, 3.3, 4 or 2. The most significant part is fixed. In this way,
 * you can say that you're willing to accept any version of a specific major
 * version, in this case version 3, that is less than a specific minor and micro
 * version, in this case 3.2.2. In other words, match any version 3 release that
 * is less than 3.2.1.
 * <p>
 * To match any version at all less than 3.2.2, regardless of major version,
 * one would apply the sign to the most significant version part; -3.2.2.
 * 
 * @author Alan Gutierrez
 */
public class VersionSelector {
    /** Match a version part and capture the sign, if any. */
    private final static Pattern VERSION_PART = Pattern.compile("([-+]?)\\d+\\.?");

    /** The underlying selection strategy, partial or exact. */
    private final SelectionStrategy selector;

    /**
     * Create a version selector with the given version selection
     * <code>pattern</code>. See the documentation of this class for examples of
     * valid version selection patterns.
     * 
     * @param pattern
     *            The version selection pattern.
     */
    public VersionSelector(String pattern) {
        Matcher matcher = VERSION_PART.matcher(pattern.trim());
        SelectionStrategy choose = null;
        int signs = 0;
        while (matcher.regionStart() < pattern.length() && matcher.lookingAt()) {
            if (matcher.group(1).length() == 1) {
                signs++;
            }
            matcher.region(matcher.end(), pattern.length());
        }
        if (matcher.hitEnd() && signs == 1) {
            choose = new RelativeSelector(pattern);
        }
        if (choose == null) {
            choose = new ExactSelector(pattern);
        }
        this.selector = choose;
    }

    /**
     * Return the greatest version number from the list of version numbers that
     * matches the version selection pattern or null if none of the version
     * numbers match.
     * 
     * @param versions
     *            The list of versions.
     * @return The greatest version that matches or null.
     */
    public String select(String...versions) {
        return selector.select(versions);
    }
}
