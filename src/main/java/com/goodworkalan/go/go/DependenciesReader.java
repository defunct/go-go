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
public class DependenciesReader {
    /** Map of respoitory names to repository classes. */
    private final static Map<String, Class<? extends Repository>> repositories = new HashMap<String, Class<? extends Repository>>();
    
    static {
        repositories.put("flat", FlatRepository.class);
        repositories.put("maven", MavenRepository.class);
    };
    
    private final Repository repository;
    
    private final List<Artifact> artifacts = new ArrayList<Artifact>();

    public DependenciesReader(File file) {
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
            String line = lines.readLine();
            while (repositoryClass == null && (line = lines.readLine()) != null) {
                line = line.trim();
                if (!(line.startsWith("#") || line.equals(""))) {
                    String[] repository = line.split("\\s+");
                    if (repository.length != 2) {
                        throw new GoException(0);
                    }
                    repositoryClass = repositories.get(repository[0]);
                    if (repositoryClass == null) {
                        throw new GoException(0);
                    }
                    try {
                        uri = new URI(repository[1]);
                    } catch (URISyntaxException e) {
                        throw new GoException(0, e);
                    }
                    if (!uri.isAbsolute()) {
                        throw new GoException(0);
                    }
                }
            }
            if (repositoryClass == null) {
                throw new GoException(0);
            }
            while ((line = lines.readLine()) != null) {
                line = line.trim();
                if ((!line.startsWith("#") || line.equals(""))) {
                    String[] artifact = line.split("\\s+");
                    if (artifact.length != 3) {
                        throw new GoException(0);
                    }
                    artifacts.add(new Artifact(artifact[0], artifact[1], artifact[2]));
                }
            }
        } catch (IOException e) {
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
    }
    
    public Repository getRepository() {
        return repository;
    }
    
    public List<Artifact> getArtifacts() {
        return artifacts;
    }
}
