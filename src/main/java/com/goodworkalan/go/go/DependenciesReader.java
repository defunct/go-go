package com.goodworkalan.go.go;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * A file reader that reads a list of dependencies from a file that 
 * contains a repository name and the dependency files.
 * 
 * @author Alan Gutierrez
 */
public class DependenciesReader {
    /** Map of respoitory names to repository classes. */
    private final static Map<String, Boolean> repositories = new HashMap<String, Boolean>();
    
    static {
        repositories.put("flat", true);
        repositories.put("maven", true);
    };

    public DependenciesReader(File file) {
        if (!file.exists()) {
            throw new GoException(0);
        }
        try {
            BufferedReader lines;
            try {
                lines = new BufferedReader(new FileReader(file));
            } catch (FileNotFoundException e) {
                throw new GoException(0, e);
            }
            String found = null;
            String line = lines.readLine();
            while (found == null && (line = lines.readLine()) != null) {
                line = line.trim();
                if (!(line.startsWith("#") || line.equals(""))) {
                    String[] repository = line.split("\\s+");
                    if (repository.length != 2) {
                        throw new GoException(0);
                    }
                    if (!repositories.containsKey(repository[0])) {
                        throw new GoException(0);
                    }
                    URI uri;
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
            if (found == null) {
                throw new GoException(0);
            }
            while ((line = lines.readLine()) != null) {
                line = line.trim();
                if ((!line.startsWith("#") || line.equals(""))) {
                    String[] dependency = line.split("\\s+");
                    if (dependency.length != 3) {
                        throw new GoException(0);
                    }
                }
            }
        } catch (IOException e) {
            throw new GoException(0, e);
        }
    }
}
