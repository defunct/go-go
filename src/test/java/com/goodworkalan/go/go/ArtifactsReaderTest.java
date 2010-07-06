package com.goodworkalan.go.go;

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
    
    // TODO Document.
    @Test
    public void fileNotFound() {
        new GoExceptionCatcher(GoException.ARTIFACT_FILE_NOT_FOUND, new Runnable() {
            public void run() {
                Artifacts.read(new File("file/not/found.txt"));
            }
        }).run();
    }
    
    // TODO Document.
    @Test
    public void firstCharacterTooLong() {
        new GoExceptionCatcher(GoException.INVALID_ARTIFACTS_LINE_START, new Runnable() {
            public void run() {
                Artifacts.read(new StringReader("xx"));
            }
        }).run();
    }
    
    // TODO Document.
    @Test
    public void invalidFirstCharacter() {
        new GoExceptionCatcher(GoException.INVALID_ARTIFACTS_LINE_START, new Runnable() {
            public void run() {
                Artifacts.read(new StringReader("&"));
            }
        }).run();
    }

    // TODO Document.
    @Test
    public void invalidIncludeLine() {
        new GoExceptionCatcher(GoException.INVALID_INCLUDE_LINE, new Runnable() {
            public void run() {
                Artifacts.read(new StringReader("+"));
            }
        }).run();
    }
    
    // TODO Document.
    @Test
    public void ioException() {
        new GoExceptionCatcher(GoException.ARTIFACT_FILE_IO_EXCEPTION, new Runnable() {
            public void run() {
                Artifacts.read(new StringReader("") {
                    @Override
                    public int read(char[] cbuf, int off, int len)
                    throws IOException {
                        throw new IOException();
                    }
                });
            }
        }).run();
    }
    
    // TODO Document.
    @Test
    public void skipComment() {
        assertEquals(Artifacts.read(new StringReader("#")).size(), 0);
    }
    
    // TODO Document.
    @Test
    public void skipBlankLines() {
        assertEquals(Artifacts.read(new StringReader("\n\n")).size(), 0);
    }
    
    // TODO Document.
    @Test
    public void read() {
        Reader reader = new InputStreamReader(getClass().getResourceAsStream("example.dep"));
        Artifacts.read(reader);
    }
}
