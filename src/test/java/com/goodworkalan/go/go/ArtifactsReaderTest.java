package com.goodworkalan.go.go;

import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import org.testng.annotations.Test;

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
        new Artifacts();;
    }
    
    @Test
    public void fileNotFound() {
        new GoExceptionCatcher(GoException.ARTIFACT_FILE_NOT_FOUND, new Runnable() {
            public void run() {
                Artifacts.read(new File("file/not/found.txt"));
            }
        }).run();
    }
    
    @Test
    public void firstCharacterTooLong() {
        new GoExceptionCatcher(GoException.INVALID_ARTIFACTS_LINE_START, new Runnable() {
            public void run() {
                Artifacts.read(new StringReader("xx"));
            }
        }).run();
    }
    
    @Test
    public void invalidFirstCharacter() {
        new GoExceptionCatcher(GoException.INVALID_ARTIFACTS_LINE_START, new Runnable() {
            public void run() {
                Artifacts.read(new StringReader("&"));
            }
        }).run();
    }

    @Test
    public void invalidIncludeLine() {
        new GoExceptionCatcher(GoException.INVALID_INCLUDE_LINE, new Runnable() {
            public void run() {
                Artifacts.read(new StringReader("+"));
            }
        }).run();
    }
    
    @Test
    public void excludeIncludeLine() {
        new GoExceptionCatcher(GoException.INVALID_EXCLUDE_LINE, new Runnable() {
            public void run() {
                Artifacts.read(new StringReader("-"));
            }
        }).run();
    }
    
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
    
    @Test
    public void excludesOnly() {
        new GoExceptionCatcher(GoException.ARTIFACT_FILE_MISPLACED_EXCLUDE, new Runnable() {
            public void run() {
                Artifacts.read(new StringReader("- com.goodworkalan/go-go/1.2.8"));
            }
        }).run();
    }
    
    @Test
    public void skipComment() {
        assertEquals(Artifacts.read(new StringReader("#")).size(), 0);
    }
    
    @Test
    public void skipBlankLines() {
        assertEquals(Artifacts.read(new StringReader("\n\n")).size(), 0);
    }
}
