package com.goodworkalan.go.go;

import static com.goodworkalan.go.go.ExcludeTest.exceptional;
import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import org.testng.annotations.Test;

import com.goodworkalan.go.go.library.Artifacts;
/**
 * Test suite for artifacts file.
 *
 * @author Alan Gutierrez
 */
public class ArtifactsReaderTest {
    /**
     * Check default constructor.
     */
    @Test
    public void defaultConstructor() {
        new Artifacts();
    }
    
    /** Test file not found exception handling. */
    @Test(expectedExceptions = GoException.class)
    public void fileNotFound() {
        exceptional(GoException.ARTIFACT_FILE_NOT_FOUND, new Runnable() {
            public void run() {
                Artifacts.read(new File("file/not/found.txt"));
            }
        });
    }
    
    /**
     * Test an artifact line with an initial character that is longer than one
     * character.
     */
    @Test(expectedExceptions = GoException.class)
    public void firstCharacterTooLong() {
        exceptional(GoException.INVALID_ARTIFACTS_LINE_START, new Runnable() {
            public void run() {
                Artifacts.read(new StringReader("xx"));
            }
        });
    }
    
    /** Test an invalidate state characater. */
    @Test(expectedExceptions = GoException.class)
    public void invalidFirstCharacter() {
        exceptional(GoException.INVALID_ARTIFACTS_LINE_START, new Runnable() {
            public void run() {
                Artifacts.read(new StringReader("&"));
            }
        });
    }

    /** Test an invalid include line. */
    @Test(expectedExceptions = GoException.class)
    public void invalidIncludeLine() {
        exceptional(GoException.INVALID_INCLUDE_LINE, new Runnable() {
            public void run() {
                Artifacts.read(new StringReader("+"));
            }
        });
    }
    
    /** Test I/O exception handling. */
    @Test(expectedExceptions = GoException.class)
    public void ioException() {
        exceptional(GoException.ARTIFACT_FILE_IO_EXCEPTION, new Runnable() {
            public void run() {
                Artifacts.read(new StringReader("") {
                    @Override
                    public int read(char[] cbuf, int off, int len)
                    throws IOException {
                        throw new IOException();
                    }
                });
            }
        });
    }
    
    /** Test skipping comments. */
    @Test
    public void skipComment() {
        assertEquals(Artifacts.read(new StringReader("#")).size(), 0);
    }
    
    /** Test skipping blank lines. */
    @Test
    public void skipBlankLines() {
        assertEquals(Artifacts.read(new StringReader("\n\n")).size(), 0);
    }
    
    /** Test a successful read. */
    @Test
    public void read() {
        Reader reader = new InputStreamReader(getClass().getResourceAsStream("example.dep"));
        Artifacts.read(reader);
    }
}
