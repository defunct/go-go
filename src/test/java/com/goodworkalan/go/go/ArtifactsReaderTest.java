package com.goodworkalan.go.go;

import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

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
    public void invalidRepositoryLine() {
        new GoExceptionCatcher(GoException.INVALID_REPOSITORY_LINE, new Runnable() {
            public void run() {
                Artifacts.read(new StringReader("?"));
            }
        }).run();
    }
    
    @Test
    public void invalidRepositoryURL() {
        new GoExceptionCatcher(GoException.INVALID_REPOSITORY_URL, new Runnable() {
            public void run() {
                Artifacts.read(new StringReader("? maven ::"));
            }
        }).run();
    }
    
    @Test
    public void relativeRepositoryURL() {
        new GoExceptionCatcher(GoException.RELATIVE_REPOSITORY_URL, new Runnable() {
            public void run() {
                Artifacts.read(new StringReader("? maven hello"));
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
    public void multipleTransactions() {
        assertEquals(Artifacts.read(new File("src/test/resources/multiple_repositories.txt")).size(), 2);
    }
    
    @Test
    public void excludesOnly() {
        assertEquals(Artifacts.read(new StringReader("- com.goodworkalan go-go 1.2.8")).size(), 0);
    }
    
    @Test
    public void skipComment() {
        List<Transaction> transactions = Artifacts.read(new StringReader("#"));
        assertEquals(transactions.size(), 0);
    }
    
    @Test
    public void skipBlankLines() {
        List<Transaction> transactions = Artifacts.read(new StringReader("\n\n"));
        assertEquals(transactions.size(), 0);
    }
}
