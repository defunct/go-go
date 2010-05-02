package com.goodworkalan.go.go;

import static org.testng.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import com.goodworkalan.go.go.library.DirectoryPart;
import com.goodworkalan.go.go.library.PathPart;
import com.goodworkalan.go.go.library.PathParts;

/**
 * Unit tests for the {@PathParts} class.
 *
 * @author Alan Gutierrez
 */
public class PathPartsTest {
    /** Test file generation. */
    @Test
    public void files() {
        List<PathPart> parts = new ArrayList<PathPart>();
        parts.add(new DirectoryPart(new File("classes")));
        File[] files = PathParts.files(parts);
        assertEquals(files[0], new File("classes"));
    }
}
