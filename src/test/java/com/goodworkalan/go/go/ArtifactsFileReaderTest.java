package com.goodworkalan.go.go;

import static org.testng.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;

import com.goodworkalan.reflective.Constructor;
import com.goodworkalan.reflective.Method;
import com.goodworkalan.reflective.ReflectiveException;
import com.goodworkalan.reflective.ReflectiveFactory;

/**
 * Test suite for artifacts file.
 *
 * @author Alan Gutierrez
 */
public class ArtifactsFileReaderTest {
    /**
     * Check default constructor.
     */
    @Test
    public void defaultConstructor() {
        new ArtifactsReader();;
    }
    
    @Test
    public void fileNotFound() {
        new GoExceptionCatcher(GoException.ARTIFACT_FILE_NOT_FOUND, new Runnable() {
            public void run() {
                new ArtifactsReader().read(new File("file/not/found.txt"));
            }
        }).run();
    }
    
    @Test
    public void firstCharacterTooLong() {
        new GoExceptionCatcher(GoException.INVALID_ARTIFACTS_LINE_START, new Runnable() {
            public void run() {
                new ArtifactsReader().read(new StringReader("xx"));
            }
        }).run();
    }
    
    @Test
    public void invalidFirstCharacter() {
        new GoExceptionCatcher(GoException.INVALID_ARTIFACTS_LINE_START, new Runnable() {
            public void run() {
                new ArtifactsReader().read(new StringReader("&"));
            }
        }).run();
    }

    @Test
    public void invalidRepositoryLine() {
        new GoExceptionCatcher(GoException.INVALID_REPOSITORY_LINE, new Runnable() {
            public void run() {
                new ArtifactsReader().read(new StringReader("?"));
            }
        }).run();
    }
    

    @Test
    public void invalidRepositoryType() {
        new GoExceptionCatcher(GoException.INVALID_REPOSITORY_TYPE, new Runnable() {
            public void run() {
                new ArtifactsReader().read(new StringReader("? fred http://kiloblog.com/fred"));
            }
        }).run();
    }
    
    @Test
    public void invalidRepositoryURL() {
        new GoExceptionCatcher(GoException.INVALID_REPOSITORY_URL, new Runnable() {
            public void run() {
                new ArtifactsReader().read(new StringReader("? maven ::"));
            }
        }).run();
    }
    
    @Test
    public void relativeRepositoryURL() {
        new GoExceptionCatcher(GoException.RELATIVE_REPOSITORY_URL, new Runnable() {
            public void run() {
                new ArtifactsReader().read(new StringReader("? maven hello"));
            }
        }).run();
    }

    @Test
    public void noRepositoryConstructor() {
        new GoExceptionCatcher(GoException.REPOSITORY_HAS_NO_URI_CONSTRUCTOR, new Runnable() {
            public void run() {
                ReflectiveFactory reflectiveFactory = new ReflectiveFactory() {
                    public <T> Method getMethod(Class<T> type, String name, Class<?>... parameterTypes) throws ReflectiveException {
                        return null;
                    }
                    
                    public <T> Constructor<T> getConstructor(Class<T> type, Class<?>... initargs) throws ReflectiveException {
                        throw new ReflectiveException(ReflectiveException.SECURITY, new SecurityException("Error"));
                    }
                }; 
                new ArtifactsReader(reflectiveFactory).read(new StringReader("? maven http://repository.com"));
            }
        }).run();
    }
    
    @Test
    public void repositoryConstructorException() {
        new GoExceptionCatcher(GoException.UNABLE_TO_CONSTRUCT_REPOSITORY, new Runnable() {
            public void run() {
                ReflectiveFactory reflectiveFactory = new ReflectiveFactory() {
                    public <T> Method getMethod(Class<T> type, String name, Class<?>... parameterTypes) throws ReflectiveException {
                        return null;
                    }
                    
                    public <T> Constructor<T> getConstructor(Class<T> type, Class<?>... initargs) throws ReflectiveException {
                        throw new ReflectiveException(ReflectiveException.ILLEGAL_ARGUMENT, new IllegalArgumentException("Error"));
                    }
                }; 

                new ArtifactsReader(reflectiveFactory).read(new StringReader("? maven http://repository.com"));
            }
        }).run();
    }
    
    @Test
    public void invalidIncludeLine() {
        new GoExceptionCatcher(GoException.INVALID_INCLUDE_LINE, new Runnable() {
            public void run() {
                new ArtifactsReader().read(new StringReader("+"));
            }
        }).run();
    }
    
    @Test
    public void excludeIncludeLine() {
        new GoExceptionCatcher(GoException.INVALID_EXCLUDE_LINE, new Runnable() {
            public void run() {
                new ArtifactsReader().read(new StringReader("-"));
            }
        }).run();
    }
    
    @Test
    public void ioException() {
        new GoExceptionCatcher(GoException.ARTIFACT_FILE_IO_EXCEPTION, new Runnable() {
            public void run() {
                new ArtifactsReader().read(new StringReader("") {
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
    public void mapConstructor() {
        new ArtifactsReader(Collections.<String, Class<? extends Repository>>emptyMap());
    }
    
    @Test
    public void multipleTransactions() {
        assertEquals(new ArtifactsReader().read(new File("src/test/resources/multiple_repositories.txt")).size(), 2);
    }
    
    @Test
    public void excludesOnly() {
        assertEquals(new ArtifactsReader().read(new StringReader("- com.goodworkalan go-go 1.2.8")).size(), 0);
    }
    
    @Test
    public void skipComment() {
        List<Transaction> transactions = new ArtifactsReader().read(new StringReader("#"));
        assertEquals(transactions.size(), 0);
    }
    
    @Test
    public void skipBlankLines() {
        List<Transaction> transactions = new ArtifactsReader().read(new StringReader("\n\n"));
        assertEquals(transactions.size(), 0);
    }
}
