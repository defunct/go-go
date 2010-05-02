package com.goodworkalan.go.go;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

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
}
