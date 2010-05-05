package com.goodworkalan.go.go.version;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.testng.annotations.Test;

/**
 * Unit tests for {@link VersionSelector}.
 *
 * @author Alan Gutierrez
 */
public class VersionSelectorTest {
    /** Test selection. */
    @Test
    public void choose() {
        String[] versions = new String[] {
            "0.1", "0.2", "0.2.2", "0.2.3", "1.0", "1.1", "2", "2-beta", 
            "3", "3.1", "3.1.1", "3.1.2", "3.2", "3.2.4", "3.2.1", "3.1.14", "3.2.3",
            "3.3.5", "3.3.6"
        };
        assertEquals(new VersionSelector("0.2.+2").select("0.2.2", "0.2.3", "3.2.2"), "0.2.3");
        assertEquals(new VersionSelector("0.2.+2").select(versions), "0.2.3");
        assertNull(new VersionSelector("1.3").select(versions));
        assertEquals(new VersionSelector("2-beta").select(versions), "2-beta");
        assertEquals(new VersionSelector("1.0").select(versions), "1.0");
        assertEquals(new VersionSelector("3.+0").select("3.2.4", "3.1.14"), "3.2.4");
        assertEquals(new VersionSelector("3.+0").select(versions), "3.3.6");
        assertEquals(new VersionSelector("3.-1").select("3", "3.1", "3.1.14"), "3.1");
        assertEquals(new VersionSelector("3.-1").select(versions), "3.1");
        assertEquals(new VersionSelector("3.-3").select("3.2", "3.2.1"), "3.2.1");
        assertEquals(new VersionSelector("-2").select(versions), "2");
        assertNull(new VersionSelector("3.3.-4").select(versions));
        assertEquals(new VersionSelector("3.+2.4").select("3.2.5"), "3.2.5");
        assertEquals(new VersionSelector("3.+2.4").select("3.2.5", "3.2.4"), "3.2.5");
        assertEquals(new VersionSelector("0.+1").select("0.1.3", "0.1.3.1"), "0.1.3.1");
        assertEquals(new VersionSelector("0.+1.4").select("0.1.4.3",  "0.1.4.4",  "0.1.4.5", "0.1.4"), "0.1.4.5");
    }

    /**
     * Test a relative selector that actually selects exactly in order to test a
     * branch within the select method that is never reached via the public or
     * protected interfaces.
     */ 
    @Test
    public void exactRelative() {
        assertEquals(new RelativeSelector("3.2.4").select("3.2.4"), "3.2.4");
    }
}
