package com.goodworkalan.go.go;

import static com.goodworkalan.go.go.GoException.ARTIFACT_FILE_IO_EXCEPTION;
import static com.goodworkalan.go.go.GoException.ARTIFACT_FILE_NOT_FOUND;
import static com.goodworkalan.go.go.GoException.INVALID_ARTIFACTS_LINE_START;
import static com.goodworkalan.go.go.GoException.INVALID_EXCLUDE_LINE;
import static com.goodworkalan.go.go.GoException.INVALID_INCLUDE_LINE;
import static com.goodworkalan.go.go.GoException.INVALID_REPOSITORY_LINE;
import static com.goodworkalan.go.go.GoException.INVALID_REPOSITORY_TYPE;
import static com.goodworkalan.go.go.GoException.INVALID_REPOSITORY_URL;
import static com.goodworkalan.go.go.GoException.RELATIVE_REPOSITORY_URL;
import static com.goodworkalan.go.go.GoException.REPOSITORY_HAS_NO_URI_CONSTRUCTOR;
import static com.goodworkalan.go.go.GoException.UNABLE_TO_CONSTRUCT_REPOSITORY;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.goodworkalan.cassandra.Report;
import com.goodworkalan.reflective.ReflectiveException;
import com.goodworkalan.reflective.ReflectiveFactory;

/**
 * A file reader that reads a list of dependencies from a file that 
 * contains a repository name and the dependency files.
 * 
 * @author Alan Gutierrez
 */
public class ArtifactsReader {
    /** Map of repository names to repository classes. */
    private final Map<String, Class<? extends Repository>> repositoryClasses = new HashMap<String, Class<? extends Repository>>();
    
    private final ReflectiveFactory reflectiveFactory;
    
    ArtifactsReader(ReflectiveFactory reflectiveFactory) {
        repositoryClasses.put("flat", FlatRepository.class);
        repositoryClasses.put("maven", MavenRepository.class);
        this.reflectiveFactory = reflectiveFactory;
    }
    
    public ArtifactsReader() {
        this(new ReflectiveFactory());
    }
    
    public ArtifactsReader(Map<String, Class<? extends Repository>> repositoryClasses) {
        repositoryClasses.putAll(repositoryClasses);
        reflectiveFactory = new ReflectiveFactory();
    }

    public List<Transaction> read(File file) {
        try {
            try {
                return read(new FileReader(file));
            } catch (FileNotFoundException e) {
                throw new GoException(ARTIFACT_FILE_NOT_FOUND, e);
            }
        } catch (GoException e) {
            e.put("file", file);
            throw e;
        }
    }
     
    public List<Transaction> read(Reader reader) {
        try {
            Report report = new Report();
            BufferedReader lines = new BufferedReader(reader);
            List<Transaction> transactions = new ArrayList<Transaction>();
            Class<? extends Repository> repositoryClass = null;
            URI uri = null;
            List<Repository> repositories = new ArrayList<Repository>();
            List<Artifact> includes = new ArrayList<Artifact>();
            List<Artifact> excludes = new ArrayList<Artifact>();
            int lineNumber = 0;
            String line;
            while ((line = lines.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (!(line.startsWith("#") || line.equals(""))) {
                    String[] split = line.split("\\s+");

                    report
                        .mark()
                        .put("line", line)
                        .put("lineNumber", lineNumber)
                        .put("startCharacter", split[0]);

                    if (split[0].length() != 1) {
                        throw new GoException(INVALID_ARTIFACTS_LINE_START, report);
                    }
                    
                    switch (split[0].charAt(0)) {
                    case '?':
                        if (split.length != 3) {
                            throw new GoException(INVALID_REPOSITORY_LINE, report);
                        }

                        report
                            .mark()
                            .map("repository")
                                .put("type", split[1])
                                .put("url", split[2])
                                .end();
                        
                        if (!includes.isEmpty()) {
                            transactions.add(new Transaction(new ArrayList<Repository>(repositories), new ArrayList<Artifact>(includes)));
                            repositories.clear();
                            includes.clear();
                            excludes.clear();
                        }
                        repositoryClass = repositoryClasses.get(split[1]);
                        if (repositoryClass == null) {
                            throw new GoException(INVALID_REPOSITORY_TYPE, report);
                        }
                        try {
                            uri = new URI(split[2]);
                        } catch (URISyntaxException e) {
                            throw new GoException(INVALID_REPOSITORY_URL, report);
                        }
                        if (!uri.isAbsolute()) {
                            throw new GoException(RELATIVE_REPOSITORY_URL, report);
                        }

                        report.mark().put("repositoryClass", repositoryClass);
                        
                        Repository repository;
                        try {
                            repository =  reflectiveFactory.getConstructor(repositoryClass, URI.class).newInstance(uri);
                        } catch (ReflectiveException e) {
                            switch (e.getCode() / 100) {
                            case ReflectiveException.CANNOT_FIND:
                                throw new GoException(REPOSITORY_HAS_NO_URI_CONSTRUCTOR, report, e);
                            default:
                                throw new GoException(UNABLE_TO_CONSTRUCT_REPOSITORY, report, e);
                            }
                        }
                        repositories.add(repository);

                        report.clear();
                        report.clear();
                        
                        break;
                    case '+':
                        if (split.length != 4) {
                            throw new GoException(INVALID_INCLUDE_LINE, report);
                        }
                        includes.add(new Artifact(split[1], split[2], split[3]));
                        break;
                    case '-':
                        if (split.length != 4) {
                            throw new GoException(INVALID_EXCLUDE_LINE, report);
                        }
                        excludes.add(new Artifact(split[1], split[1], split[3]));
                        break;
                    default:
                        throw new GoException(INVALID_ARTIFACTS_LINE_START, report);
                    }
                    report.clear();
                }
            }
            if (!includes.isEmpty()) {
                transactions.add(new Transaction(repositories, includes));
            }
            return transactions;
        } catch (IOException e) {
            throw new GoException(ARTIFACT_FILE_IO_EXCEPTION, e);
        }
    }
}
