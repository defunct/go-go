package com.goodworkalan.go.go;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

/**
 * Unit tests for the {@link ArgumentList} class.
 *
 * @author Alan Gutierrez
 */
public class ArgumentListTest {
    /** Test the copy constructor. */
    @Test
    public void constructor() {
        List<String> args = new ArrayList<String>();
        args.add("--happy=true");
        assertEquals(new ArgumentList(args).size(), 1);
    }
    
    /** Test removing an argument. */
    @Test
    public void removeArgument() {
        ArgumentList arguments = new ArgumentList();
        arguments.addArgument("snap:mississippi", "true");
        arguments.addArgument("button:saratoga", "foo");
        assertEquals(arguments.size(), 2);
        assertNull(arguments.removeArgument("foo:bar"));
        assertEquals(arguments.removeArgument("button:saratoga"), "foo");
        assertEquals(arguments.size(), 1);
    }
    
    /** Test getting an argument. */
    @Test
    public void getArgument() {
        ArgumentList arguments = new ArgumentList();
        arguments.addArgument("snap:mississippi", "true");
        arguments.addArgument("button:saratoga", "foo");
        assertEquals(arguments.size(), 2);
        assertNull(arguments.getArgument("foo:bar"));
        assertEquals(arguments.getArgument("button:saratoga"), "foo");
        assertEquals(arguments.size(), 2);
    }
}
