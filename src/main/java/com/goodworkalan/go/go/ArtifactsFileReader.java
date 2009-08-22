package com.goodworkalan.go.go;

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
    
    private final List<Transaction> transactions = new ArrayList<Transaction>();

    public ArtifactsFileReader(File file) {
        if (!file.exists()) {
            throw new GoException(0);
        }
        Class<? extends Repository> repositoryClass = null;
        URI uri = null;
        try {
            BufferedReader lines;
            try {
                lines = new BufferedReader(new FileReader(file));
            } catch (FileNotFoundException e) {
                throw new GoException(0, e);
            }
            List<Repository> repositories = new ArrayList<Repository>();
            List<Artifact> artifacts = new ArrayList<Artifact>();
            String line = lines.readLine();
            while ((line = lines.readLine()) != null) {
                line = line.trim();
                if (!(line.startsWith("#") || line.equals(""))) {
                    String[] split = line.split("\\s+");
                    if (split.length == 2) {
                        if (!artifacts.isEmpty()) {
                            transactions.add(new Transaction(new ArrayList<Repository>(repositories), new ArrayList<Artifact>(artifacts)));
                            repositories.clear();
                            artifacts.clear();
                        }
                        repositoryClass = repositoryClasses.get(split[0]);
                        if (repositoryClass == null) {
                            throw new GoException(0);
                        }
                        try {
                            uri = new URI(split[1]);
                        } catch (URISyntaxException e) {
                            throw new GoException(0, e);
                        }
                        Constructor<?> constructor;
                        try {
                            constructor = repositoryClass.getConstructor(URI.class);
                        } catch (RuntimeException e) {
                            throw e;
                        } catch (Exception e) {
                            throw new GoException(0, e);
                        }
                        Repository repository;
                        try {
                            repository = (Repository) constructor.newInstance(uri);
                            // TODO WOW! I never noticed all this before! Need to write about it.
                        } catch (IllegalArgumentException e) {
                            throw new GoException(0, e);
                        } catch (RuntimeException e) {
                            throw e;
                        } catch (Exception e) {
                            throw new GoException(0, e);
                        }
                        repositories.add(repository);
                    } else if (split.length == 3) {
                        artifacts.add(new Artifact(split[0], split[1], split[2]));
                    } else {
                        throw new GoException(0);
                    }
                    if (!uri.isAbsolute()) {
                        throw new GoException(0);
                    }
                }
            }
            if (!artifacts.isEmpty()) {
                transactions.add(new Transaction(repositories, artifacts));
            }
        } catch (IOException e) {
            throw new GoException(0, e);
        }
       
        
    }
    
    public List<Transaction> getTransactions() {
        return transactions;
    }
}
