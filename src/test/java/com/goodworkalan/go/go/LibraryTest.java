package com.goodworkalan.go.go;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import java.io.File;

import org.testng.annotations.Test;

/**
 * Unit test for the library class.
 * 
 * @author Alan Gutierrez
 */
public class LibraryTest {
    /**
     * Test the library equality and hash code.
     */
    @Test
    public void equality() {
        Library library = new Library(new File("/lib"));
        assertEquals(library, new Library(new File("/lib")));
        assertFalse(library.equals("/lib"));
        assertEquals(library.hashCode(), new Library(new File("/lib")).hashCode());
    }
}
