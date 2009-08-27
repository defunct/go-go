package com.goodworkalan.go.go;

import static com.goodworkalan.go.go.GoException.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.goodworkalan.cassandra.Report;

/**
 * A file reader that reads a list of dependencies from a file that 
 * contains a repository name and the dependency files.
 * 
 * @author Alan Gutierrez
 */
public class ArtifactsFileReader {
    /** Map of respoitory names to repository classes. */
    private final static Map<String, Class<? extends Repository>> repositoryClasses = new HashMap<String, Class<? extends Repository>>();
    
    static {
        repositoryClasses.put("flat", FlatRepository.class);
        repositoryClasses.put("maven", MavenRepository.class);
    };
    
    /** The a set of artifacts and a list of repositories to query. */
    private final List<Transaction> transactions = new ArrayList<Transaction>();

    public ArtifactsFileReader(File file) {
        if (!file.exists()) {
            throw new GoException(0);
        }
        Class<? extends Repository> repositoryClass = null;
        URI uri = null;
        Report report = new Report();
        report.put("file", file);
        try {
            BufferedReader lines;
            try {
                lines = new BufferedReader(new FileReader(file));
            } catch (FileNotFoundException e) {
                throw new GoException(0, e);
            }
            List<Repository> repositories = new ArrayList<Repository>();
            List<Artifact> includes = new ArrayList<Artifact>();
            List<Artifact> excludes = new ArrayList<Artifact>();
            String line;
            while ((line = lines.readLine()) != null) {
                line = line.trim();
                if (!(line.startsWith("#") || line.equals(""))) {
                    report.mark().put("line", line);

                    String[] split = line.split("\\s+");
                    if (split[0].length() != 1) {
                        throw new GoException(INVALID_ARTIFACT_LINE_START, report);
                    }
                    
                    switch (split[0].charAt(0)) {
                    case '?':
                        if (split.length != 3) {
                            throw new GoException(INVALID_ARTIFACT_REPOSITORY_LINE, report);
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
                        }
                        repositoryClass = repositoryClasses.get(split[1]);
                        if (repositoryClass == null) {
                            throw new GoException(INVALID_ARTIFACT_REPOSITORY_TYPE, report);
                        }
                        try {
                            uri = new URI(split[2]);
                        } catch (URISyntaxException e) {
                            throw new GoException(INVALID_ARTIFACT_REPOSITORY_URL, report);
                        }
                        if (!uri.isAbsolute()) {
                            throw new GoException(RELATIVE_ARTIFACT_REPOSITORY_URL, report);
                        }

                        report.mark().put("repositoryClass", repositoryClass);
                        
                        Constructor<?> constructor;
                        try {
                            constructor = repositoryClass.getConstructor(URI.class);
                        } catch (RuntimeException e) {
                            throw e;
                        } catch (Exception e) {
                            throw new GoException(UNABLE_TO_FIND_REPOSITORY_CONSTRUCTOR, report, e);
                        }
                        Repository repository;
                        try {
                            repository = (Repository) constructor.newInstance(uri);
                            // TODO WOW! I never noticed all this before! Need to write about it.
                        } catch (IllegalArgumentException e) {
                            throw new GoException(UNABLE_TO_CONSTRUCT_REPOSITORY, report, e);
                        } catch (RuntimeException e) {
                            throw e;
                        } catch (Exception e) {
                            throw new GoException(UNABLE_TO_CONSTRUCT_REPOSITORY, report, e);
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
                    case '-':
                        if (split.length != 4) {
                            throw new GoException(INVALID_INCLUDE_LINE, report);
                        }
                        excludes.add(new Artifact(split[1], split[1], split[3]));
                    default:
                        throw new GoException(0);
                    }
                    report.clear();
                }
            }
            if (!includes.isEmpty()) {
                transactions.add(new Transaction(repositories, includes));
            }
        } catch (IOException e) {
            // Note that build as you throw will not capture the properties
            // in the loop above.
            throw new GoException(0, e);
        }
    }
    
    public List<Transaction> getTransactions() {
        return transactions;
    }
}
