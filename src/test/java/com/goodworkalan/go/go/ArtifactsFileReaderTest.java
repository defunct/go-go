package com.goodworkalan.go.go;

import static org.testng.Assert.*;
import java.io.File;
import java.io.StringReader;
import java.util.List;

import org.testng.annotations.Test;

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
                ArtifactsReader.read(new File("file/not/found.txt"));
            }
        }).run();
    }
    
    @Test
    public void firstCharacterTooLong() {
        new GoExceptionCatcher(GoException.INVALID_ARTIFACTS_LINE_START, new Runnable() {
            public void run() {
                ArtifactsReader.read(new StringReader("xx"));
            }
        }).run();
    }
    
    @Test
    public void invalidFirstCharacter() {
        new GoExceptionCatcher(GoException.INVALID_ARTIFACTS_LINE_START, new Runnable() {
            public void run() {
                ArtifactsReader.read(new StringReader("&"));
            }
        }).run();
    }

    @Test
    public void invalidRepositoryLine() {
        new GoExceptionCatcher(GoException.INVALID_REPOSITORY_LINE, new Runnable() {
            public void run() {
                ArtifactsReader.read(new StringReader("?"));
            }
        }).run();
    }
    

    @Test
    public void invalidRepositoryType() {
        new GoExceptionCatcher(GoException.INVALID_REPOSITORY_TYPE, new Runnable() {
            public void run() {
                ArtifactsReader.read(new StringReader("? fred http://kiloblog.com/fred"));
            }
        }).run();
    }
    
    @Test
    public void invalidRepositoryURL() {
        new GoExceptionCatcher(GoException.INVALID_REPOSITORY_URL, new Runnable() {
            public void run() {
                ArtifactsReader.read(new StringReader("? maven ::"));
            }
        }).run();
    }
    
    @Test
    public void relativeRepositoryURL() {
        new GoExceptionCatcher(GoException.RELATIVE_REPOSITORY_URL, new Runnable() {
            public void run() {
                ArtifactsReader.read(new StringReader("? maven hello"));
            }
        }).run();
    }

    @Test
    public void noRepositoryConstructor() {
        new GoExceptionCatcher(GoException.REPOSITORY_HAS_NO_URI_CONSTRUCTOR, new Runnable() {
            public void run() {
                ArtifactsReader.repositoryClasses.put("noConstructor", NoConstructorRepository.class);
                ArtifactsReader.read(new StringReader("? noConstructor http://repository.com"));
            }
        }).run();
    }
    
    @Test
    public void repositoryConstructorException() {
        new GoExceptionCatcher(GoException.UNABLE_TO_CONSTRUCT_REPOSITORY, new Runnable() {
            public void run() {
                ArtifactsReader.repositoryClasses.put("exceptional", ExceptionRaisingRepository.class);
                ArtifactsReader.read(new StringReader("? exceptional http://repository.com"));
            }
        }).run();
    }
    
    @Test
    public void invalidIncludeLine() {
        new GoExceptionCatcher(GoException.INVALID_INCLUDE_LINE, new Runnable() {
            public void run() {
                ArtifactsReader.read(new StringReader("+"));
            }
        }).run();
    }
    
    
    @Test
    public void excludeIncludeLine() {
        new GoExceptionCatcher(GoException.INVALID_EXCLUDE_LINE, new Runnable() {
            public void run() {
                ArtifactsReader.read(new StringReader("-"));
            }
        }).run();
    }
    
    @Test
    public void multipleTransactions() {
        assertEquals(ArtifactsReader.read(new File("src/test/resources/multiple_repositories.txt")).size(), 2);
    }
    
    @Test
    public void excludesOnly() {
        assertEquals(ArtifactsReader.read(new StringReader("- com.goodworkalan go-go 1.2.8")).size(), 0);
    }
    
    @Test
    public void skipComment() {
        List<Transaction> transactions = ArtifactsReader.read(new StringReader("#"));
        assertEquals(transactions.size(), 0);
    }
    
    @Test
    public void skipBlankLines() {
        List<Transaction> transactions = ArtifactsReader.read(new StringReader("\n\n"));
        assertEquals(transactions.size(), 0);
    }
}
